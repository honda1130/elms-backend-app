package com.everrefine.elms.presentation.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.everrefine.elms.application.dto.PasswordResetTokenDeletionDto;
import com.everrefine.elms.application.service.PasswordResetApplicationService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * {@link DeleteExpiredPasswordResetTokensScheduler} のテスト。
 *
 * <p>スケジューラーはアプリケーションサービスへの委譲とログ出力のみを行い、DBを使用しないため、サービスをモックにした単体テストとする。
 */
@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class DeleteExpiredPasswordResetTokensSchedulerTest {

  @Mock private PasswordResetApplicationService passwordResetApplicationService;

  @InjectMocks private DeleteExpiredPasswordResetTokensScheduler scheduler;

  @Nested
  class 期限切れトークン削除 {

    @Test
    void 開始ログと削除件数を含む終了ログが出力されること(CapturedOutput output) {
      when(passwordResetApplicationService.deleteExpiredTokens(any()))
          .thenReturn(new PasswordResetTokenDeletionDto(3));

      scheduler.deleteExpiredPasswordResetTokens();

      assertTrue(output.getOut().contains("期限切れパスワード再設定トークンの削除バッチを実行します。基準時刻: "));
      assertTrue(output.getOut().contains("期限切れパスワード再設定トークンの削除バッチの実行が完了しました。削除件数: 3件"));
    }

    @Test
    void 削除件数が0件でも終了ログが出力されること(CapturedOutput output) {
      when(passwordResetApplicationService.deleteExpiredTokens(any()))
          .thenReturn(new PasswordResetTokenDeletionDto(0));

      scheduler.deleteExpiredPasswordResetTokens();

      assertTrue(output.getOut().contains("期限切れパスワード再設定トークンの削除バッチの実行が完了しました。削除件数: 0件"));
    }

    @Test
    void 開始ログに出力した基準時刻がサービスに渡されること(CapturedOutput output) {
      when(passwordResetApplicationService.deleteExpiredTokens(any()))
          .thenReturn(new PasswordResetTokenDeletionDto(0));

      scheduler.deleteExpiredPasswordResetTokens();

      ArgumentCaptor<LocalDateTime> baseTimeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
      verify(passwordResetApplicationService).deleteExpiredTokens(baseTimeCaptor.capture());
      assertTrue(output.getOut().contains("基準時刻: " + baseTimeCaptor.getValue()));
    }

    @Test
    void 例外が発生したときERRORログが出力され例外が再送出されないこと(CapturedOutput output) {
      when(passwordResetApplicationService.deleteExpiredTokens(any()))
          .thenThrow(new DataAccessResourceFailureException("DB接続失敗"));

      assertDoesNotThrow(() -> scheduler.deleteExpiredPasswordResetTokens());

      assertTrue(
          output
              .getOut()
              .lines()
              .anyMatch(
                  line ->
                      line.contains("ERROR")
                          && line.contains("期限切れパスワード再設定トークンの削除バッチの実行に失敗しました。")));
      assertTrue(output.getOut().contains("DataAccessResourceFailureException"));
    }
  }
}
