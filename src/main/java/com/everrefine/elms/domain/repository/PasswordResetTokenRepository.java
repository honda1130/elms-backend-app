package com.everrefine.elms.domain.repository;

import com.everrefine.elms.domain.model.passwordreset.PasswordResetToken;
import java.time.LocalDateTime;
import java.util.Optional;

/** パスワードリセットトークンのリポジトリインターフェース。 */
public interface PasswordResetTokenRepository {

  /**
   * パスワードリセットトークンを保存する。
   *
   * @param token 保存するパスワードリセットトークン
   * @return 保存されたパスワードリセットトークン
   */
  PasswordResetToken save(PasswordResetToken token);

  /**
   * トークン文字列でパスワードリセットトークンを取得する。
   *
   * @param token トークン文字列
   * @return パスワードリセットトークン（存在しない場合は空）
   */
  Optional<PasswordResetToken> findByToken(String token);

  /**
   * 有効期限が基準時刻より前のパスワードリセットトークンを、利用済み・未使用にかかわらず削除する。
   *
   * <p>有効期限が基準時刻と同一のトークンは削除しない。{@link PasswordResetToken#isExpired()} の判定と一致させるため。
   *
   * @param baseTime 基準時刻
   * @return 削除件数
   */
  int deleteExpiredTokens(LocalDateTime baseTime);
}
