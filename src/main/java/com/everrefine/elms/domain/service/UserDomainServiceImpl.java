package com.everrefine.elms.domain.service;

import com.everrefine.elms.domain.model.user.User;
import com.everrefine.elms.domain.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/** {@link UserDomainService} の実装。 */
@Component
@RequiredArgsConstructor
public class UserDomainServiceImpl implements UserDomainService {

  private final UserRepository userRepository;

  /**
   * ログインユーザーをUser型のインスタンスとして取得します。
   *
   * @return ログインユーザー
   */
  @Override
  public Optional<User> findLoginUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.empty();
    }

    if (!(authentication.getPrincipal() instanceof UserDetails userDetails)) {
      return Optional.empty();
    }

    UUID userId;
    try {
      userId = UUID.fromString(userDetails.getUsername());
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
    return userRepository.findUserById(userId);
  }
}
