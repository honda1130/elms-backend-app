package com.everrefine.elms.infrastructure.repository;

import com.everrefine.elms.domain.model.featureflag.FeatureFlag;
import com.everrefine.elms.domain.model.featureflag.FeatureFlagKey;
import com.everrefine.elms.domain.repository.FeatureFlagRepository;
import com.everrefine.elms.infrastructure.dao.FeatureFlagDao;
import com.everrefine.elms.infrastructure.entity.featureflag.FeatureFlagEntity;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

/** {@link FeatureFlagRepository} の実装。 */
@Repository
@AllArgsConstructor
public class FeatureFlagRepositoryImpl implements FeatureFlagRepository {

  private final FeatureFlagDao featureFlagDao;

  @Override
  public Optional<FeatureFlag> findByKey(FeatureFlagKey featureFlagKey) {
    return featureFlagDao
        .findByFeatureFlagKey(featureFlagKey.value())
        .map(FeatureFlagEntity::toDomain);
  }

  @Override
  public FeatureFlag createFeatureFlag(FeatureFlag featureFlag) {
    return featureFlagDao.save(FeatureFlagEntity.from(featureFlag)).toDomain();
  }

  @Override
  public FeatureFlag updateFeatureFlag(FeatureFlag featureFlag) {
    return featureFlagDao.save(FeatureFlagEntity.from(featureFlag)).toDomain();
  }
}
