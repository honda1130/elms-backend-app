package com.everrefine.elms.application.service;

/**
 * メール送信アプリケーションサービスのインターフェース。
 *
 * <p>送信元アドレス・本文テンプレート・送信処理をこのサービスに集約し、 呼び出し側は「どのメールを誰に送るか」だけを指定する。
 */
public interface MailApplicationService {

  /**
   * パスワード再設定のご案内メールを送信する。
   *
   * @param to 送信先メールアドレス
   * @param token パスワードリセットトークン
   */
  void sendPasswordResetEmail(String to, String token);

  /**
   * パスワード再設定の完了メールを送信する。
   *
   * @param to 送信先メールアドレス
   */
  void sendPasswordResetCompleteEmail(String to);

  /**
   * アカウント作成のお知らせ（ウェルカム）メールを送信する。
   *
   * @param to 送信先メールアドレス
   * @param userName ユーザー名
   */
  void sendWelcomeEmail(String to, String userName);
}
