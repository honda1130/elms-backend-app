package com.everrefine.elms.infrastructure.dao;

import com.everrefine.elms.infrastructure.entity.tag.TagEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/** タグのDAOインターフェース。 */
public interface TagDao extends CrudRepository<TagEntity, UUID> {

  Optional<TagEntity> findByName(String name);

  @Query("""
      SELECT t.id, t.name
      FROM tags t
      INNER JOIN lesson_tags lt
        ON t.id = lt.tag_id
      WHERE lt.lesson_id = :lessonId
      ORDER BY t.name
      """)
  List<TagEntity> findByLessonId(@Param("lessonId") UUID lessonId);
}
