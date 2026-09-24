package com.everrefine.elms.application.service;

import com.everrefine.elms.application.command.FeatureFlagUpdateCommand;
import com.everrefine.elms.application.dto.FeatureFlagDto;

/** フィーチャーフラグアプリケーションサービスのインターフェース。 */
public interface FeatureFlagApplicationService {

  /**
   * キーを指定してフィーチャーフラグの状態を取得する。
   *
   * @param featureFlagKey フィーチャーフラグのキー
   * @return フィーチャーフラグDTO。キーが未登録の場合は無効状態のDTO
   */
  FeatureFlagDto getFeatureFlag(String featureFlagKey);

  /**
   * フィーチャーフラグの有効状態を更新する。キーが未登録の場合は新規登録する。
   *
   * @param featureFlagUpdateCommand フィーチャーフラグ更新コマンド
   * @return 登録または更新後のフィーチャーフラグDTO
   */
  FeatureFlagDto updateFeatureFlag(FeatureFlagUpdateCommand featureFlagUpdateCommand);
}
