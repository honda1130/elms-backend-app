package com.everrefine.elms.infrastructure.repository;

import com.everrefine.elms.domain.model.tag.Tag;
import com.everrefine.elms.domain.model.tag.TagName;
import com.everrefine.elms.domain.repository.TagRepository;
import com.everrefine.elms.infrastructure.dao.TagDao;
import com.everrefine.elms.infrastructure.entity.tag.TagEntity;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class TagRepositoryImpl implements TagRepository {

  private final TagDao tagDao;
  private final JdbcAggregateTemplate jdbcAggregateTemplate;

  @Override
  public List<Tag> findByNameIn(List<TagName> tagNames) {
    List<String> names = toNames(tagNames);
    if (names.isEmpty()) {
      return List.of();
    }
    return tagDao.findByNameIn(names).stream().map(TagEntity::toDomain).toList();
  }

  @Override
  public List<Tag> createTags(List<TagName> tagNames) {
    List<String> names = toNames(tagNames);
    if (names.isEmpty()) {
      return List.of();
    }

    List<TagEntity> tagEntities =
        names.stream().map(name -> new TagEntity(UUID.randomUUID(), name)).toList();
    jdbcAggregateTemplate.insertAll(tagEntities);
    return tagEntities.stream().map(TagEntity::toDomain).toList();
  }

  private List<String> toNames(List<TagName> tagNames) {
    return tagNames.stream().distinct().map(TagName::value).toList();
  }
}
