package com.everrefine.elms.domain.model.featureflag;

/**
 * アプリケーションで参照するフィーチャーフラグの一覧。
 *
 * <p>キー文字列を各所で個別に定義すると、重複や書き間違いに気付けないため、ここに集約する。
 */
public enum FeatureFlagName {

  /** ユーザー作成時のウェルカムメール送信。 */
  WELCOME_MAIL("welcome-mail");

  private final String key;

  FeatureFlagName(String key) {
    this.key = key;
  }

  /**
   * フィーチャーフラグのキーを取得する。
   *
   * @return フィーチャーフラグのキー
   */
  public String key() {
    return key;
  }
}
