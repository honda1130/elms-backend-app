package com.everrefine.elms.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * {@link MailApplicationService} の実装。
 *
 * <p>件名と本文は {@link MailTemplate} が持ち、このクラスは送信元アドレスの付与と {@link JavaMailSender} への受け渡しを担う。
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
    send(to, MailTemplate.PASSWORD_RESET, resetLink);
  }

  @Override
  public void sendPasswordResetCompleteEmail(String to) {
    send(to, MailTemplate.PASSWORD_RESET_COMPLETE, to);
  }

  /**
   * 送信元アドレスを付与してメールを送信する。
   *
   * @param to 送信先メールアドレス
   * @param template 使用するメールテンプレート
   * @param args 本文テンプレートに差し込む値
   */
  private void send(String to, MailTemplate template, Object... args) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(to);
    message.setSubject(template.getSubject());
    message.setText(template.buildBody(args));
    mailSender.send(message);
  }
}
