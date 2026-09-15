package com.everrefine.elms.application.service;

/**
 * メールの件名と本文テンプレートを表す列挙型。
 *
 * <p>件名と本文は必ず対で使われるため、両者を1つの定数にまとめて一覧できるようにしている。 本文中の {@code %s} は {@link #buildBody} の引数で差し込む。
 */
public enum MailTemplate {

  /** パスワード再設定のご案内。本文にはリセットリンクを差し込む。 */
  PASSWORD_RESET(
      "【Javaエンジニア養成講座】パスワード再設定のご案内",
      """
      Javaエンジニア養成講座をご利用いただきありがとうございます。

      パスワード再設定のリクエストを受け付けました。
      以下のリンクをクリックして、新しいパスワードを設定してください。

      %s

      ※ このリンクは発行から30分間有効です。
      ※ ご自身でリクエストしていない場合は、このメールを無視してください。

      ──────────────────────────────
      Javaエンジニア養成講座
      """),

  /** パスワード再設定の完了通知。本文には対象のメールアドレスを差し込む。 */
  PASSWORD_RESET_COMPLETE(
      "【Javaエンジニア養成講座】パスワード再設定が完了しました",
      """
      Javaエンジニア養成講座をご利用いただきありがとうございます。

      以下のアカウントのパスワード再設定が完了しました。

      メールアドレス：%s

      ※ ご自身で操作していない場合は、お問い合わせください。

      ──────────────────────────────
      Javaエンジニア養成講座
      """);

  private final String subject;
  private final String bodyTemplate;

  /**
   * メールテンプレートのコンストラクタ。
   *
   * @param subject 件名
   * @param bodyTemplate 本文テンプレート
   */
  MailTemplate(String subject, String bodyTemplate) {
    this.subject = subject;
    this.bodyTemplate = bodyTemplate;
  }

  /**
   * 件名を返す。
   *
   * @return 件名
   */
  public String getSubject() {
    return subject;
  }

  /**
   * 本文テンプレートに値を差し込んだ本文を返す。
   *
   * @param args 本文テンプレートの {@code %s} に差し込む値
   * @return 本文
   */
  public String buildBody(Object... args) {
    return bodyTemplate.formatted(args);
  }
}
