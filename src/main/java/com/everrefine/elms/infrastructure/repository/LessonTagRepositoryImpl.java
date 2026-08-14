package com.everrefine.elms.infrastructure.repository;

import com.everrefine.elms.domain.model.tag.Tag;
import com.everrefine.elms.domain.repository.LessonTagRepository;
import com.everrefine.elms.infrastructure.dao.LessonTagDao;
import com.everrefine.elms.infrastructure.entity.tag.LessonTagEntity;
import com.everrefine.elms.infrastructure.row.LessonTagRow;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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
  public List<Tag> findTagsByLessonId(UUID lessonId) {
    return lessonTagDao.findTagsByLessonId(lessonId).stream().map(LessonTagRow::toTag).toList();
  }

  @Override
  public Map<UUID, List<Tag>> findTagsByLessonIdIn(List<UUID> lessonIds) {
    if (lessonIds == null || lessonIds.isEmpty()) {
      return Map.of();
    }

    return lessonTagDao.findTagsByLessonIdIn(lessonIds).stream()
        .collect(
            Collectors.groupingBy(
                LessonTagRow::lessonId,
                LinkedHashMap::new,
                Collectors.mapping(LessonTagRow::toTag, Collectors.toList())));
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
