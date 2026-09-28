package com.everrefine.elms.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * {@link MailApplicationServiceImpl} の統合テストクラス。
 *
 * <p>本文は集約前の実装が実際に送信していた内容をそのまま期待値としている。 リファクタリングによって文面が変わっていないことを保証するため、部分一致ではなく全文で比較する。
 */
@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = WebEnvironment.NONE) // WebまわりのConfigurationはBean生成を無効にして高速化する。
@Testcontainers // DBはDockerコンテナを使用する。
@Transactional // 各テストメソッド終了時にテストデータをロールバックする。
class MailApplicationServiceImplTest {

  /** テストで使うDBを用意する。 */
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17").withReuse(true);

  /** テスト対象のサービスクラス。 */
  @Autowired private MailApplicationService mailApplicationService;

  /**
   * メール送信はSMTPサーバーへの外部依存のため差し替える。
   *
   * <p>インターフェースではなく実装クラスを差し替えているのは、Actuatorのメールヘルスチェックが {@code JavaMailSenderImpl}
   * 型のBeanを要求しており、インターフェースで上書きすると起動に失敗するため。
   */
  @MockitoBean private JavaMailSenderImpl mailSender;

  /** 送信されたメールを取得する。 */
  private SimpleMailMessage captureSentMessage() {
    ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(captor.capture());
    return captor.getValue();
  }

  @Nested
  class パスワード再設定案内メール送信 {

    @Test
    void 送信元と宛先と件名が設定されること() {
      mailApplicationService.sendPasswordResetEmail("user@example.com", "test-token");

      SimpleMailMessage message = captureSentMessage();
      assertEquals("test@example.com", message.getFrom());
      assertEquals("user@example.com", String.join(",", message.getTo()));
      assertEquals("【Javaエンジニア養成講座】パスワード再設定のご案内", message.getSubject());
    }

    @Test
    void 本文がリセットリンクを含む定型文であること() {
      mailApplicationService.sendPasswordResetEmail("user@example.com", "test-token");

      assertEquals(
          """
          Javaエンジニア養成講座をご利用いただきありがとうございます。

          パスワード再設定のリクエストを受け付けました。
          以下のリンクをクリックして、新しいパスワードを設定してください。

          http://localhost:3000/reset-password?token=test-token

          ※ このリンクは発行から30分間有効です。
          ※ ご自身でリクエストしていない場合は、このメールを無視してください。

          ──────────────────────────────
          Javaエンジニア養成講座
          """,
          captureSentMessage().getText());
    }
  }

  @Nested
  class パスワード再設定完了メール送信 {

    @Test
    void 送信元と宛先と件名が設定されること() {
      mailApplicationService.sendPasswordResetCompleteEmail("user@example.com");

      SimpleMailMessage message = captureSentMessage();
      assertEquals("test@example.com", message.getFrom());
      assertEquals("user@example.com", String.join(",", message.getTo()));
      assertEquals("【Javaエンジニア養成講座】パスワード再設定が完了しました", message.getSubject());
    }

    @Test
    void 本文が宛先メールアドレスを含む定型文であること() {
      mailApplicationService.sendPasswordResetCompleteEmail("user@example.com");

      assertEquals(
          """
          Javaエンジニア養成講座をご利用いただきありがとうございます。

          以下のアカウントのパスワード再設定が完了しました。

          メールアドレス：user@example.com

          ※ ご自身で操作していない場合は、お問い合わせください。

          ──────────────────────────────
          Javaエンジニア養成講座
          """,
          captureSentMessage().getText());
    }
  }

  @Nested
  class ウェルカムメール送信 {

    @Test
    void 送信元と宛先と件名が設定されること() {
      mailApplicationService.sendWelcomeEmail("user@example.com", "yamada_taro");

      SimpleMailMessage message = captureSentMessage();
      assertEquals("test@example.com", message.getFrom());
      assertEquals("user@example.com", String.join(",", message.getTo()));
      assertEquals("【Javaエンジニア養成講座】アカウント作成のお知らせ", message.getSubject());
    }

    @Test
    void 本文がユーザー名とメールアドレスを含む定型文であること() {
      mailApplicationService.sendWelcomeEmail("user@example.com", "yamada_taro");

      assertEquals(
          """
          Javaエンジニア養成講座をご利用いただきありがとうございます。

          アカウントが作成されました。

          ユーザー名：yamada_taro
          メールアドレス：user@example.com

          ──────────────────────────────
          Javaエンジニア養成講座
          """,
          captureSentMessage().getText());
    }
  }
}
