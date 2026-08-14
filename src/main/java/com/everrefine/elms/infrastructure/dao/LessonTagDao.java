package com.everrefine.elms.infrastructure.dao;

import com.everrefine.elms.infrastructure.entity.tag.LessonTagEntity;
import com.everrefine.elms.infrastructure.row.LessonTagRow;
import java.util.List;
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
  @Query(
      """
      DELETE FROM lesson_tags
      WHERE lesson_id = :lessonId
      """)
  void deleteByLessonId(@Param("lessonId") UUID lessonId);

  @Query(
      """
      SELECT
        lt.lesson_id AS lesson_id,
        t.id AS tag_id,
        t.name AS tag_name
      FROM lesson_tags lt
      INNER JOIN tags t
        ON t.id = lt.tag_id
      WHERE lt.lesson_id = :lessonId
      ORDER BY t.name ASC, t.id ASC
      """)
  List<LessonTagRow> findTagsByLessonId(@Param("lessonId") UUID lessonId);

  @Query(
      """
      SELECT
        lt.lesson_id AS lesson_id,
        t.id AS tag_id,
        t.name AS tag_name
      FROM lesson_tags lt
      INNER JOIN tags t
        ON t.id = lt.tag_id
      WHERE lt.lesson_id IN (:lessonIds)
      ORDER BY lt.lesson_id ASC, t.name ASC, t.id ASC
      """)
  List<LessonTagRow> findTagsByLessonIdIn(@Param("lessonIds") List<UUID> lessonIds);
}
