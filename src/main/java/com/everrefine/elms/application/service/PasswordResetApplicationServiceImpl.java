package com.everrefine.elms.application.service;

import com.everrefine.elms.application.command.PasswordResetConfirmCommand;
import com.everrefine.elms.application.command.PasswordResetRequestCommand;
import com.everrefine.elms.application.exception.BadRequestException;
import com.everrefine.elms.domain.exception.InvalidValueException;
import com.everrefine.elms.domain.model.passwordreset.PasswordResetToken;
import com.everrefine.elms.domain.model.user.EmailAddress;
import com.everrefine.elms.domain.model.user.User;
import com.everrefine.elms.domain.repository.PasswordResetTokenRepository;
import com.everrefine.elms.domain.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** パスワードリセットアプリケーションサービスの実装。 */
@Service
@RequiredArgsConstructor
public class PasswordResetApplicationServiceImpl implements PasswordResetApplicationService {

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final MailApplicationService mailApplicationService;

  @Override
  @Transactional
  public void requestPasswordReset(PasswordResetRequestCommand command) {
    Optional<User> userOpt;
    try {
      userOpt = userRepository.findUserByEmailAddress(new EmailAddress(command.emailAddress()));
    } catch (InvalidValueException e) {
      return;
    }

    if (userOpt.isEmpty()) {
      return;
    }

    User user = userOpt.get();
    PasswordResetToken resetToken = PasswordResetToken.create(user.id());
    passwordResetTokenRepository.save(resetToken);
    mailApplicationService.sendPasswordResetEmail(command.emailAddress(), resetToken.token());
  }

  @Override
  @Transactional
  public String confirmPasswordReset(PasswordResetConfirmCommand command) {
    PasswordResetToken resetToken =
        passwordResetTokenRepository
            .findByToken(command.token())
            .orElseThrow(() -> new BadRequestException("無効なトークンです"));

    if (resetToken.isExpired()) {
      throw new BadRequestException("トークンの有効期限が切れています");
    }

    if (resetToken.isUsed()) {
      throw new BadRequestException("このトークンはすでに使用されています");
    }

    // password_reset_tokens.user_id は users(id) への外部キー（ON DELETE CASCADE）のため、
    // 現行のDBスキーマではユーザーが存在しないトークンは発生しない。将来スキーマが変わった場合に備えた防御的な分岐。
    User user =
        userRepository
            .findUserById(resetToken.userId())
            .orElseThrow(() -> new BadRequestException("ユーザーが見つかりません"));

    String emailAddress = user.emailAddress().value();
    userRepository.updateUser(user.update(null, command.newPassword(), null, null, null, null));
    passwordResetTokenRepository.save(resetToken.markAsUsed());
    mailApplicationService.sendPasswordResetCompleteEmail(emailAddress);

    return emailAddress;
  }
}
