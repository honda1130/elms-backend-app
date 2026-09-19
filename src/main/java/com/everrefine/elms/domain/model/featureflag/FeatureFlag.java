package com.everrefine.elms.domain.model.featureflag;

import java.util.UUID;

/** フィーチャーフラグのドメインモデル。 */
public record FeatureFlag(UUID id, FeatureFlagKey featureFlagKey, boolean enabled) {

  public FeatureFlag(UUID id, String key, boolean enabled) {
    this(id, new FeatureFlagKey(key), enabled);
  }

  /**
   * フィーチャーフラグのキーを取得する。
   *
   * @return フィーチャーフラグのキー
   */
  public String key() {
    return featureFlagKey.value();
  }
}
