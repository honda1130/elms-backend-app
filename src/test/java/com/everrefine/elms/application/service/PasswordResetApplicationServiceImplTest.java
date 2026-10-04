package com.everrefine.elms.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.everrefine.elms.application.command.PasswordResetConfirmCommand;
import com.everrefine.elms.application.command.PasswordResetRequestCommand;
import com.everrefine.elms.application.dto.PasswordResetTokenDeletionDto;
import com.everrefine.elms.application.exception.BadRequestException;
import com.everrefine.elms.testsupport.TestDataFactory;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = WebEnvironment.NONE) // WebまわりのConfigurationはBean生成を無効にして高速化する。
@Testcontainers // DBはDockerコンテナを使用する。
@Transactional // 各テストメソッド終了時にテストデータをロールバックする。
class PasswordResetApplicationServiceImplTest {

  /** テストで使うDBを用意する。 */
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17").withReuse(true);

  /** テスト対象のサービスクラス。 */
  @Autowired private PasswordResetApplicationServiceImpl passwordResetApplicationService;

  /** データ検証で使用するためのJdbcTemplate。 */
  @Autowired private JdbcTemplate jdbcTemplate;

  /** テストデータ作成ヘルパー。 */
  @Autowired private TestDataFactory testData;

  /**
   * メール送信はSMTPサーバーへの外部依存であり、DBの検証とは無関係のため差し替える。
   *
   * <p>インターフェースではなく実装クラスを差し替えているのは、Actuatorのメールヘルスチェックが {@code JavaMailSenderImpl}
   * 型のBeanを要求しており、インターフェースで上書きすると起動に失敗するため。
   */
  @MockitoBean private JavaMailSenderImpl mailSender;

  /** リセットトークンの件数を取得する。 */
  private int countTokens() {
    return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM password_reset_tokens", Integer.class);
  }

  @Nested
  class パスワードリセット申請 {

    /**
     * 形式が不正なメールアドレスを渡しても例外にならないことを検証する。
     *
     * <p>{@code EmailAddress} が投げるのは {@code InvalidValueException} であり、{@code
     * IllegalArgumentException} ではない。catchする型を誤ると例外がそのまま伝播し、クライアント起因の誤りが500として返ってしまう。
     * このテストはその退行を検出する。
     */
    @Test
    void 形式が不正なメールアドレスでも例外が投げられないこと() {
      int before = countTokens();

      assertDoesNotThrow(
          () ->
              passwordResetApplicationService.requestPasswordReset(
                  new PasswordResetRequestCommand("invalid-email")));

      assertEquals(before, countTokens());
      verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void 存在しないメールアドレスでも例外が投げられずトークンも作成されないこと() {
      int before = countTokens();

      assertDoesNotThrow(
          () ->
              passwordResetApplicationService.requestPasswordReset(
                  new PasswordResetRequestCommand("notfound@example.com")));

      assertEquals(before, countTokens());
      verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void 登録済みのメールアドレスならトークンが作成されること() {
      UUID userId =
          testData.createUser("reset@example.com", "password123", "山田 太郎", "yamada", "GENERAL");

      passwordResetApplicationService.requestPasswordReset(
          new PasswordResetRequestCommand("reset@example.com"));

      LocalDateTime expiresAt =
          jdbcTemplate.queryForObject(
              "SELECT expires_at FROM password_reset_tokens WHERE user_id = ?",
              LocalDateTime.class,
              userId);
      assertNotNull(expiresAt);
      verify(mailSender).send(any(SimpleMailMessage.class));
    }
  }

  @Nested
  class パスワードリセット確定 {

    @Test
    void トークンが存在しないときBadRequestExceptionが投げられること() {
      PasswordResetConfirmCommand command =
          new PasswordResetConfirmCommand("missing-token", "newPass123");

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> passwordResetApplicationService.confirmPasswordReset(command));

      assertEquals("無効なトークンです", exception.getMessage());
    }

    @Test
    void トークンが期限切れのときBadRequestExceptionが投げられること() {
      UUID userId =
          testData.createUser("expired@example.com", "password123", "山田 太郎", "yamada", "GENERAL");
      testData.createPasswordResetToken(
          userId, "expired-token", LocalDateTime.now().minusMinutes(1), null);
      PasswordResetConfirmCommand command =
          new PasswordResetConfirmCommand("expired-token", "newPass123");

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> passwordResetApplicationService.confirmPasswordReset(command));

      assertEquals("トークンの有効期限が切れています", exception.getMessage());
    }

    @Test
    void トークンが使用済みのときBadRequestExceptionが投げられること() {
      UUID userId =
          testData.createUser("used@example.com", "password123", "山田 太郎", "yamada", "GENERAL");
      testData.createPasswordResetToken(
          userId, "used-token", LocalDateTime.now().plusMinutes(10), LocalDateTime.now());
      PasswordResetConfirmCommand command =
          new PasswordResetConfirmCommand("used-token", "newPass123");

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> passwordResetApplicationService.confirmPasswordReset(command));

      assertEquals("このトークンはすでに使用されています", exception.getMessage());
    }

    @Test
    void 有効なトークンならパスワードが更新されトークンが使用済みになること() {
      UUID userId =
          testData.createUser("active@example.com", "password123", "山田 太郎", "yamada", "GENERAL");
      String oldPassword =
          jdbcTemplate.queryForObject(
              "SELECT password FROM users WHERE id = ?", String.class, userId);
      testData.createPasswordResetToken(
          userId, "active-token", LocalDateTime.now().plusMinutes(10), null);
      PasswordResetConfirmCommand command =
          new PasswordResetConfirmCommand("active-token", "newPass123");

      String emailAddress = passwordResetApplicationService.confirmPasswordReset(command);

      assertEquals("active@example.com", emailAddress);
      String newPassword =
          jdbcTemplate.queryForObject(
              "SELECT password FROM users WHERE id = ?", String.class, userId);
      assertNotNull(newPassword);
      assertNotEquals(oldPassword, newPassword);
      LocalDateTime usedAt =
          jdbcTemplate.queryForObject(
              "SELECT used_at FROM password_reset_tokens WHERE token = ?",
              LocalDateTime.class,
              "active-token");
      assertNotNull(usedAt);
    }

    @Test
    void トークンが未使用ならused_atがnullのままであること() {
      UUID userId =
          testData.createUser("keep@example.com", "password123", "山田 太郎", "yamada", "GENERAL");
      testData.createPasswordResetToken(
          userId, "keep-token", LocalDateTime.now().minusMinutes(1), null);

      assertThrows(
          BadRequestException.class,
          () ->
              passwordResetApplicationService.confirmPasswordReset(
                  new PasswordResetConfirmCommand("keep-token", "newPass123")));

      LocalDateTime usedAt =
          jdbcTemplate.queryForObject(
              "SELECT used_at FROM password_reset_tokens WHERE token = ?",
              LocalDateTime.class,
              "keep-token");
      assertNull(usedAt);
    }
  }

  @Nested
  class 期限切れトークン削除 {

    /** 削除の基準時刻。境界値を検証するため固定値にする。 */
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 1, 1, 0, 30);

    /** 指定したトークン文字列のレコードが存在するかを取得する。 */
    private boolean existsToken(String token) {
      Integer count =
          jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM password_reset_tokens WHERE token = ?", Integer.class, token);
      return count != null && count > 0;
    }

    @Test
    void 期限切れの未使用トークンが削除されること() {
      UUID userId =
          testData.createUser("expired@example.com", "password123", "山田 太郎", "yamada", "GENERAL");
      testData.createPasswordResetToken(
          userId, "expired-unused-token", BASE_TIME.minusSeconds(1), null);

      passwordResetApplicationService.deleteExpiredTokens(BASE_TIME);

      assertFalse(existsToken("expired-unused-token"));
    }

    @Test
    void 期限切れの使用済みトークンが削除されること() {
      UUID userId =
          testData.createUser("expired@example.com", "password123", "山田 太郎", "yamada", "GENERAL");
      testData.createPasswordResetToken(
          userId, "expired-used-token", BASE_TIME.minusSeconds(1), BASE_TIME.minusMinutes(10));

      passwordResetApplicationService.deleteExpiredTokens(BASE_TIME);

      assertFalse(existsToken("expired-used-token"));
    }

    @Test
    void 期限内のトークンは削除されないこと() {
      UUID userId =
          testData.createUser("active@example.com", "password123", "山田 太郎", "yamada", "GENERAL");
      testData.createPasswordResetToken(userId, "active-token", BASE_TIME.plusSeconds(1), null);

      passwordResetApplicationService.deleteExpiredTokens(BASE_TIME);

      assertTrue(existsToken("active-token"));
    }

    /**
     * 有効期限が基準時刻と同一のトークンは削除されないことを検証する。
     *
     * <p>{@code PasswordResetToken#isExpired()}
     * は現在時刻が有効期限より後の場合のみ期限切れと判定するため、有効期限ちょうどのトークンはまだ有効である。
     */
    @Test
    void 有効期限が基準時刻と同一のトークンは削除されないこと() {
      UUID userId =
          testData.createUser("boundary@example.com", "password123", "山田 太郎", "yamada", "GENERAL");
      testData.createPasswordResetToken(userId, "boundary-token", BASE_TIME, null);

      passwordResetApplicationService.deleteExpiredTokens(BASE_TIME);

      assertTrue(existsToken("boundary-token"));
    }

    @Test
    void 削除した件数が返ること() {
      UUID userId =
          testData.createUser("count@example.com", "password123", "山田 太郎", "yamada", "GENERAL");
      testData.createPasswordResetToken(userId, "expired-token-1", BASE_TIME.minusMinutes(1), null);
      testData.createPasswordResetToken(
          userId, "expired-token-2", BASE_TIME.minusDays(1), BASE_TIME.minusDays(1));
      testData.createPasswordResetToken(userId, "active-token", BASE_TIME.plusMinutes(1), null);

      PasswordResetTokenDeletionDto result =
          passwordResetApplicationService.deleteExpiredTokens(BASE_TIME);

      assertEquals(2, result.deletedCount());
    }

    @Test
    void 削除対象が無い場合は0件が返ること() {
      UUID userId =
          testData.createUser("none@example.com", "password123", "山田 太郎", "yamada", "GENERAL");
      testData.createPasswordResetToken(userId, "active-token", BASE_TIME.plusMinutes(1), null);

      PasswordResetTokenDeletionDto result =
          passwordResetApplicationService.deleteExpiredTokens(BASE_TIME);

      assertEquals(0, result.deletedCount());
      assertTrue(existsToken("active-token"));
    }
  }
}
