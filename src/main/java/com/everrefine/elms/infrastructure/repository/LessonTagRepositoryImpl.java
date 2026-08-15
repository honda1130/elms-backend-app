package com.everrefine.elms.infrastructure.repository;

import com.everrefine.elms.domain.repository.LessonTagRepository;
import com.everrefine.elms.infrastructure.dao.LessonTagDao;
import com.everrefine.elms.infrastructure.entity.tag.LessonTagEntity;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Repository;

/** {@link LessonTagRepository} の実装。 */
@Repository
@AllArgsConstructor
public class LessonTagRepositoryImpl implements LessonTagRepository {

  private final LessonTagDao lessonTagDao;
  private final JdbcAggregateTemplate jdbcAggregateTemplate;

  @Override
  public void deleteByLessonId(UUID lessonId) {
    lessonTagDao.deleteByLessonId(lessonId);
  }

  @Override
  public void createLessonTags(UUID lessonId, List<UUID> tagIds) {
    List<UUID> distinctTagIds = tagIds.stream().distinct().toList();
    if (distinctTagIds.isEmpty()) {
      return;
    }

    jdbcAggregateTemplate.insertAll(
        distinctTagIds.stream()
            .map(tagId -> new LessonTagEntity(UUID.randomUUID(), lessonId, tagId))
            .toList());
  }
}
