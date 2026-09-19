package com.everrefine.elms.infrastructure.entity.featureflag;

import com.everrefine.elms.domain.model.featureflag.FeatureFlag;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** フィーチャーフラグのエンティティ。 */
@Table("feature_flags")
public record FeatureFlagEntity(
    @Id UUID id,
    String featureFlagKey,
    boolean enabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  /**
   * ドメインモデルに変換する。
   *
   * @return フィーチャーフラグのドメインモデル
   */
  public FeatureFlag toDomain() {
    return new FeatureFlag(id, featureFlagKey, enabled);
  }
}
