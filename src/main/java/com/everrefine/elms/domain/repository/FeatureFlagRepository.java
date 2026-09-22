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
}
