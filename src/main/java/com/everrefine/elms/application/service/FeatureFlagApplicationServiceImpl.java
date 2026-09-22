package com.everrefine.elms.application.service;

import com.everrefine.elms.application.command.FeatureFlagUpdateCommand;
import com.everrefine.elms.application.dto.FeatureFlagDto;
import com.everrefine.elms.domain.model.featureflag.FeatureFlag;
import com.everrefine.elms.domain.model.featureflag.FeatureFlagKey;
import com.everrefine.elms.domain.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link FeatureFlagApplicationService} の実装。 */
@Service
@RequiredArgsConstructor
public class FeatureFlagApplicationServiceImpl implements FeatureFlagApplicationService {

  private final FeatureFlagRepository featureFlagRepository;

  /**
   * キーを指定してフィーチャーフラグの状態を取得する。
   *
   * <p>未登録のキーは「無効」として扱う。フラグが未作成の状態でも呼び出し側が分岐できるようにするため、 リソース未検出の例外にはしない。
   *
   * @param featureFlagKey フィーチャーフラグのキー
   * @return フィーチャーフラグDTO。キーが未登録の場合は無効状態のDTO
   */
  @Override
  @Transactional(readOnly = true)
  public FeatureFlagDto getFeatureFlag(String featureFlagKey) {
    return featureFlagRepository
        .findByKey(new FeatureFlagKey(featureFlagKey))
        .map(FeatureFlagDto::from)
        .orElseGet(() -> FeatureFlagDto.disabled(featureFlagKey));
  }

  /**
   * フィーチャーフラグの有効状態を更新する。
   *
   * <p>キーが未登録の場合は新規登録する。フラグの事前登録なしに、管理者が再デプロイせず機能を切り替えられるようにするため、 リソース未検出の例外にはしない。
   *
   * @param featureFlagUpdateCommand フィーチャーフラグ更新コマンド
   * @return 登録または更新後のフィーチャーフラグDTO
   */
  @Override
  @Transactional
  public FeatureFlagDto updateFeatureFlag(FeatureFlagUpdateCommand featureFlagUpdateCommand) {
    FeatureFlag persistedFeatureFlag =
        featureFlagRepository
            .findByKey(new FeatureFlagKey(featureFlagUpdateCommand.featureFlagKey()))
            .map(
                featureFlag ->
                    featureFlagRepository.updateFeatureFlag(
                        featureFlagUpdateCommand.toFeatureFlag(featureFlag)))
            .orElseGet(
                () ->
                    featureFlagRepository.createFeatureFlag(
                        featureFlagUpdateCommand.toNewFeatureFlag()));
    return FeatureFlagDto.from(persistedFeatureFlag);
  }
}
