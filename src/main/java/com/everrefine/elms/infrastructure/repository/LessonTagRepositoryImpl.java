package com.everrefine.elms.infrastructure.repository;

import com.everrefine.elms.domain.repository.LessonTagRepository;
import com.everrefine.elms.infrastructure.dao.LessonTagDao;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * {@link LessonTagRepository} の実装。
 */
@Repository
@AllArgsConstructor
public class LessonTagRepositoryImpl implements LessonTagRepository {

  private final LessonTagDao lessonTagDao;

  @Override
  public void deleteByLessonId(UUID lessonId) {
    lessonTagDao.deleteByLessonId(lessonId);
  }

  @Override
  public void createLessonTag(UUID lessonId, UUID tagId) {
    lessonTagDao.create(lessonId, tagId);
  }
}
