package com.everrefine.elms.domain.model.featureflag;

import java.time.LocalDateTime;
import java.util.UUID;

/** フィーチャーフラグのドメインモデル。 */
public record FeatureFlag(
    UUID id,
    FeatureFlagKey featureFlagKey,
    boolean enabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public FeatureFlag(
      UUID id, String key, boolean enabled, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this(id, new FeatureFlagKey(key), enabled, createdAt, updatedAt);
  }

  /**
   * 新規作成用のフィーチャーフラグを作成する。
   *
   * @param key フィーチャーフラグのキー
   * @param enabled 機能が有効ならtrue
   * @return 新規作成用のフィーチャーフラグ
   */
  public static FeatureFlag create(String key, boolean enabled) {
    LocalDateTime now = LocalDateTime.now();
    return new FeatureFlag(null, key, enabled, now, now);
  }

  /**
   * 有効状態を更新したフィーチャーフラグを返す。
   *
   * @param enabled 機能が有効ならtrue
   * @return 更新後のフィーチャーフラグ
   */
  public FeatureFlag update(boolean enabled) {
    return new FeatureFlag(
        this.id, this.featureFlagKey, enabled, this.createdAt, LocalDateTime.now());
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
