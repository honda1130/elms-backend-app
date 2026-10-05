package com.everrefine.elms.application.service;

import com.everrefine.elms.application.command.PasswordResetConfirmCommand;
import com.everrefine.elms.application.command.PasswordResetRequestCommand;
import com.everrefine.elms.application.dto.PasswordResetTokenDeletionDto;
import java.time.LocalDateTime;

/** パスワードリセットアプリケーションサービスのインターフェース。 */
public interface PasswordResetApplicationService {

  /**
   * パスワードリセットをリクエストする。
   *
   * @param command パスワードリセットリクエストCommand
   */
  void requestPasswordReset(PasswordResetRequestCommand command);

  /**
   * パスワードを更新し、自動ログイン用にメールアドレスを返す。
   *
   * @param command パスワードリセット確定Command
   * @return 認証用メールアドレス
   */
  String confirmPasswordReset(PasswordResetConfirmCommand command);

  /**
   * 有効期限が基準時刻より前のパスワードリセットトークンを削除する。
   *
   * @param baseTime 基準時刻
   * @return 削除結果
   */
  PasswordResetTokenDeletionDto deleteExpiredTokens(LocalDateTime baseTime);
}
