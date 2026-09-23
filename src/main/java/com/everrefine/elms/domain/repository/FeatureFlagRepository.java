package com.everrefine.elms.domain.repository;

import com.everrefine.elms.domain.model.featureflag.FeatureFlag;
import com.everrefine.elms.domain.model.featureflag.FeatureFlagKey;
import java.util.Optional;

/** フィーチャーフラグのリポジトリインターフェース。 */
public interface FeatureFlagRepository {

  /**
   * キーでフィーチャーフラグを取得する。
   *
   * @param featureFlagKey フィーチャーフラグのキー
   * @return フィーチャーフラグ。該当するキーが登録されていない場合は空
   */
  Optional<FeatureFlag> findByKey(FeatureFlagKey featureFlagKey);

  /**
   * フィーチャーフラグを保存する。IDを持たない場合は登録し、持つ場合は更新する。
   *
   * @param featureFlag 保存するフィーチャーフラグ
   * @return 保存後のフィーチャーフラグ
   */
  FeatureFlag saveFeatureFlag(FeatureFlag featureFlag);
}
