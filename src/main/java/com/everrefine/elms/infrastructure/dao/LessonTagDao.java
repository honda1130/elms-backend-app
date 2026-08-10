package com.everrefine.elms.infrastructure.dao;

import com.everrefine.elms.infrastructure.entity.tag.LessonTagEntity;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** レッスンタグ紐付けのDAOインターフェース。 */
@Repository
public interface LessonTagDao extends CrudRepository<LessonTagEntity, UUID> {

  @Modifying
  @Query("""
      DELETE FROM lesson_tags
      WHERE lesson_id = :lessonId
      """)
  void deleteByLessonId(@Param("lessonId") UUID lessonId);

  @Modifying
  @Query("""
      INSERT INTO lesson_tags (
        lesson_id,
        tag_id
      )
      VALUES (
        :lessonId,
        :tagId
      )
      """)
  void create(@Param("lessonId") UUID lessonId, @Param("tagId") UUID tagId);
}
