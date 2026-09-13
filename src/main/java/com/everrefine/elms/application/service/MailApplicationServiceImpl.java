package com.everrefine.elms.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * {@link MailApplicationService} の実装。
 *
 * <p>送信元アドレスの付与と {@link JavaMailSender} への受け渡しを {@link #send} に集約し、 各送信メソッドは件名と本文の組み立てのみを担う。
 */
@Service
@RequiredArgsConstructor
public class MailApplicationServiceImpl implements MailApplicationService {

  private final JavaMailSender mailSender;

  @Value("${mail.from}")
  private String fromAddress;

  @Value("${password-reset.base-url}")
  private String baseUrl;

  @Override
  public void sendPasswordResetEmail(String to, String token) {
    String resetLink = baseUrl + "/reset-password?token=" + token;
    send(
        to,
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
        """
            .formatted(resetLink));
  }

  @Override
  public void sendPasswordResetCompleteEmail(String to) {
    send(
        to,
        "【Javaエンジニア養成講座】パスワード再設定が完了しました",
        """
        Javaエンジニア養成講座をご利用いただきありがとうございます。

        以下のアカウントのパスワード再設定が完了しました。

        メールアドレス：%s

        ※ ご自身で操作していない場合は、お問い合わせください。

        ──────────────────────────────
        Javaエンジニア養成講座
        """
            .formatted(to));
  }

  /**
   * 送信元アドレスを付与してメールを送信する。
   *
   * @param to 送信先メールアドレス
   * @param subject 件名
   * @param text 本文
   */
  private void send(String to, String subject, String text) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);
    mailSender.send(message);
  }
}
