package com.everrefine.elms.infrastructure.dao;

import com.everrefine.elms.infrastructure.entity.tag.TagEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/** タグのDAOインターフェース。 */
public interface TagDao extends CrudRepository<TagEntity, UUID> {

  @Query(
      """
      SELECT id, name
      FROM tags
      WHERE name IN (:names)
      """)
  List<TagEntity> findByNameIn(@Param("names") List<String> names);
}
