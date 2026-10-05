package com.everrefine.elms.infrastructure.dao;

import com.everrefine.elms.infrastructure.entity.passwordreset.PasswordResetTokenEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** パスワードリセットトークンのDAOインターフェース。 */
@Repository
public interface PasswordResetTokenDao extends CrudRepository<PasswordResetTokenEntity, UUID> {

  /**
   * トークン文字列でパスワードリセットトークンを取得する。
   *
   * @param token トークン文字列
   * @return パスワードリセットトークン（存在しない場合は空）
   */
  Optional<PasswordResetTokenEntity> findByToken(String token);

  /**
   * 有効期限が基準時刻より前のパスワードリセットトークンを削除する。
   *
   * @param baseTime 基準時刻
   * @return 削除件数
   */
  @Modifying
  @Query(
      """
      DELETE FROM password_reset_tokens
      WHERE expires_at < :baseTime
      """)
  int deleteByExpiresAtBefore(@Param("baseTime") LocalDateTime baseTime);
}
