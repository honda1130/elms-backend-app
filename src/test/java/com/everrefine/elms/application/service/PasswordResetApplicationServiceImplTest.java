package com.everrefine.elms.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.everrefine.elms.application.command.PasswordResetConfirmCommand;
import com.everrefine.elms.application.exception.BadRequestException;
import com.everrefine.elms.domain.model.passwordreset.PasswordResetToken;
import com.everrefine.elms.domain.repository.PasswordResetTokenRepository;
import com.everrefine.elms.domain.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class PasswordResetApplicationServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
  @Mock private JavaMailSender mailSender;

  @InjectMocks private PasswordResetApplicationServiceImpl passwordResetApplicationService;

  @Nested
  class パスワードリセット確定 {

    @Test
    void トークンが存在しないときBadRequestExceptionが投げられること() {
      when(passwordResetTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () ->
                  passwordResetApplicationService.confirmPasswordReset(
                      new PasswordResetConfirmCommand("missing", "newPass123")));

      assertEquals("無効なトークンです", exception.getMessage());
      verifyNoInteractions(userRepository, mailSender);
    }

    @Test
    void トークンが期限切れのときBadRequestExceptionが投げられること() {
      when(passwordResetTokenRepository.findByToken("expired"))
          .thenReturn(Optional.of(expiredToken()));

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () ->
                  passwordResetApplicationService.confirmPasswordReset(
                      new PasswordResetConfirmCommand("expired", "newPass123")));

      assertEquals("トークンの有効期限が切れています", exception.getMessage());
      verifyNoInteractions(userRepository, mailSender);
    }

    @Test
    void トークンが使用済みのときBadRequestExceptionが投げられること() {
      when(passwordResetTokenRepository.findByToken("used")).thenReturn(Optional.of(usedToken()));

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () ->
                  passwordResetApplicationService.confirmPasswordReset(
                      new PasswordResetConfirmCommand("used", "newPass123")));

      assertEquals("このトークンはすでに使用されています", exception.getMessage());
      verifyNoInteractions(userRepository, mailSender);
    }

    @Test
    void ユーザーが見つからないときBadRequestExceptionが投げられること() {
      UUID userId = UUID.randomUUID();
      when(passwordResetTokenRepository.findByToken("active"))
          .thenReturn(Optional.of(activeToken(userId)));
      when(userRepository.findUserById(userId)).thenReturn(Optional.empty());

      BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () ->
                  passwordResetApplicationService.confirmPasswordReset(
                      new PasswordResetConfirmCommand("active", "newPass123")));

      assertEquals("ユーザーが見つかりません", exception.getMessage());
      verifyNoInteractions(mailSender);
    }
  }

  private PasswordResetToken activeToken(UUID userId) {
    return new PasswordResetToken(
        UUID.randomUUID(), userId, "active", LocalDateTime.now().plusMinutes(10), null);
  }

  private PasswordResetToken expiredToken() {
    return new PasswordResetToken(
        UUID.randomUUID(), UUID.randomUUID(), "expired", LocalDateTime.now().minusMinutes(1), null);
  }

  private PasswordResetToken usedToken() {
    return new PasswordResetToken(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "used",
        LocalDateTime.now().plusMinutes(10),
        LocalDateTime.now());
  }
}
