package com.everrefine.elms.infrastructure.dao;

import com.everrefine.elms.infrastructure.entity.featureflag.FeatureFlagEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

/** フィーチャーフラグのDAOインターフェース。 */
public interface FeatureFlagDao extends CrudRepository<FeatureFlagEntity, UUID> {

  /**
   * キーでフィーチャーフラグを取得する。
   *
   * @param featureFlagKey フィーチャーフラグのキー
   * @return フィーチャーフラグのエンティティ（存在しない場合は空）
   */
  Optional<FeatureFlagEntity> findByFeatureFlagKey(String featureFlagKey);
}
