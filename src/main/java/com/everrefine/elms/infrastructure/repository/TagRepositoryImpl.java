package com.everrefine.elms.infrastructure.repository;

import com.everrefine.elms.domain.model.lesson.LessonTag;
import com.everrefine.elms.domain.repository.TagRepository;
import com.everrefine.elms.infrastructure.dao.TagDao;
import com.everrefine.elms.infrastructure.entity.tag.TagEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class TagRepositoryImpl implements TagRepository {

  private final TagDao tagDao;

  @Override
  public Optional<LessonTag> findByName(String name) {
    return tagDao.findByName(name)
        .map(TagEntity::toDomain);
  }

  @Override
  public LessonTag createTag(String name) {
    return tagDao.save(TagEntity.create(name))
        .toDomain();
  }

  @Override
  public List<LessonTag> findByLessonId(UUID lessonId) {
    return tagDao.findByLessonId(lessonId)
        .stream()
        .map(TagEntity::toDomain)
        .toList();
  }
}
