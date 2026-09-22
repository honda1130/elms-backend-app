package com.everrefine.elms.domain.model.featureflag;

/**
 * フィーチャーフラグのキーの値オブジェクト。
 *
 * <p>未登録のキーも「無効なフラグ」として受け付ける仕様のため、ここでは形式の検証を行わない。 値の形式はリクエスト側のバリデーションで担保する。
 */
public record FeatureFlagKey(String value) {

  public FeatureFlagKey {
    value = value == null ? null : value.trim();
  }
}
