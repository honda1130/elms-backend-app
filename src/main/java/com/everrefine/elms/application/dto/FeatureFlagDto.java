package com.everrefine.elms.application.dto;

import com.everrefine.elms.domain.model.featureflag.FeatureFlag;
import io.swagger.v3.oas.annotations.media.Schema;

/** フィーチャーフラグのDTO。 */
public record FeatureFlagDto(
    @Schema(description = "フィーチャーフラグのキー", example = "welcome-mail") String featureFlagKey,
    @Schema(description = "機能が有効ならtrue", example = "true") boolean enabled) {

  /**
   * フィーチャーフラグ情報からFeatureFlagDtoを生成する。
   *
   * @param featureFlag フィーチャーフラグ情報
   * @return フィーチャーフラグDTO
   */
  public static FeatureFlagDto from(FeatureFlag featureFlag) {
    return new FeatureFlagDto(featureFlag.key(), featureFlag.enabled());
  }

  /**
   * 未登録のキー用に、無効状態のFeatureFlagDtoを生成する。
   *
   * @param featureFlagKey フィーチャーフラグのキー
   * @return 無効状態のフィーチャーフラグDTO
   */
  public static FeatureFlagDto disabled(String featureFlagKey) {
    return new FeatureFlagDto(featureFlagKey, false);
  }
}
