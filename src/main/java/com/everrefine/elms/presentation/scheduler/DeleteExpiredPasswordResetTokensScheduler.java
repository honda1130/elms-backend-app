package com.everrefine.elms.presentation.scheduler;

import com.everrefine.elms.application.dto.PasswordResetTokenDeletionDto;
import com.everrefine.elms.application.service.PasswordResetApplicationService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 期限切れパスワードリセットトークン削除スケジューラー。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteExpiredPasswordResetTokensScheduler {

  private final PasswordResetApplicationService passwordResetApplicationService;

  /**
   * 期限切れのパスワードリセットトークンを削除する。
   *
   * <p>例外が発生した場合はERRORログを出力して終了し、再送出しない。再送出するとSpringのスケジューラーでもログが出力され、ERRORログが二重になるため。
   *
   * <p>例外はトランザクションの外である本クラスで捕捉する。アプリケーションサービス（トランザクション内）で捕捉して正常に戻すと、コミット時に {@code
   * UnexpectedRollbackException} が発生しうるため。
   */
  @Scheduled(cron = "0 30 0 * * *", zone = "Asia/Tokyo")
  public void deleteExpiredPasswordResetTokens() {
    LocalDateTime baseTime = LocalDateTime.now();
    log.info("期限切れパスワード再設定トークンの削除バッチを実行します。基準時刻: {}", baseTime);

    try {
      PasswordResetTokenDeletionDto result =
          passwordResetApplicationService.deleteExpiredTokens(baseTime);
      log.info("期限切れパスワード再設定トークンの削除バッチの実行が完了しました。削除件数: {}件", result.deletedCount());
    } catch (RuntimeException e) {
      log.error("期限切れパスワード再設定トークンの削除バッチの実行に失敗しました。", e);
    }
  }
}
