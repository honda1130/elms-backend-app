package com.everrefine.elms.application.command;

import com.everrefine.elms.domain.model.featureflag.FeatureFlag;

/** フィーチャーフラグ更新用のコマンド。 */
public record FeatureFlagUpdateCommand(String featureFlagKey, boolean enabled) {

  /**
   * 既存のフィーチャーフラグを更新したドメインモデルに変換する。
   *
   * @param featureFlag 更新対象のフィーチャーフラグ
   * @return 更新後のフィーチャーフラグ
   */
  public FeatureFlag toFeatureFlag(FeatureFlag featureFlag) {
    return featureFlag.update(enabled);
  }

  /**
   * 新規登録用のフィーチャーフラグに変換する。
   *
   * @return 新規登録用のフィーチャーフラグ
   */
  public FeatureFlag toNewFeatureFlag() {
    return FeatureFlag.create(featureFlagKey, enabled);
  }
}
