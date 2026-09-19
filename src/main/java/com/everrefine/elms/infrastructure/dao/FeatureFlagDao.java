package com.everrefine.elms.infrastructure.dao;

import com.everrefine.elms.infrastructure.entity.featureflag.FeatureFlagEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/** フィーチャーフラグのDAOインターフェース。 */
public interface FeatureFlagDao extends CrudRepository<FeatureFlagEntity, UUID> {

  /**
   * キーでフィーチャーフラグを取得する。
   *
   * @param featureFlagKey フィーチャーフラグのキー
   * @return フィーチャーフラグのエンティティ
   */
  @Query(
      """
      SELECT id, feature_flag_key, enabled, created_at, updated_at
      FROM feature_flags
      WHERE feature_flag_key = :featureFlagKey
      """)
  Optional<FeatureFlagEntity> findByFeatureFlagKey(@Param("featureFlagKey") String featureFlagKey);
}
