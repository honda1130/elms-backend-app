package com.everrefine.elms.presentation.request;

import com.everrefine.elms.application.command.FeatureFlagUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** フィーチャーフラグ更新リクエスト。 */
public record FeatureFlagUpdateRequest(
    @Schema(description = "機能を有効にするならtrue", example = "false")
        @NotNull(message = "フィーチャーフラグの有効状態は必須です。") Boolean enabled) {

  /**
   * Commandオブジェクトに変換する。
   *
   * @param featureFlagKey フィーチャーフラグのキー
   * @return フィーチャーフラグ更新Command
   */
  public FeatureFlagUpdateCommand toCommand(String featureFlagKey) {
    return new FeatureFlagUpdateCommand(featureFlagKey, enabled);
  }
}
