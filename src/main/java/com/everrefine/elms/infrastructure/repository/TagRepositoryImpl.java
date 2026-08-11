package com.everrefine.elms.infrastructure.repository;

import com.everrefine.elms.domain.model.tag.Tag;
import com.everrefine.elms.domain.model.tag.TagName;
import com.everrefine.elms.domain.repository.TagRepository;
import com.everrefine.elms.infrastructure.dao.TagDao;
import com.everrefine.elms.infrastructure.entity.tag.TagEntity;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class TagRepositoryImpl implements TagRepository {

  private static final RowMapper<TagEntity> TAG_ENTITY_ROW_MAPPER =
      (rs, rowNum) -> new TagEntity(rs.getObject("id", UUID.class), rs.getString("name"));

  private final TagDao tagDao;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @Override
  public List<Tag> findByNameIn(List<String> names) {
    List<String> normalizedNames = normalizeNames(names);
    if (normalizedNames.isEmpty()) {
      return List.of();
    }
    return tagDao.findByNameIn(normalizedNames).stream().map(TagEntity::toDomain).toList();
  }

  @Override
  public List<Tag> createTags(List<String> names) {
    List<String> normalizedNames = normalizeNames(names);
    if (normalizedNames.isEmpty()) {
      return List.of();
    }

    String values =
        IntStream.range(0, normalizedNames.size())
            .mapToObj(index -> "(:name" + index + ")")
            .collect(Collectors.joining(", "));
    MapSqlParameterSource params = new MapSqlParameterSource();
    IntStream.range(0, normalizedNames.size())
        .forEach(index -> params.addValue("name" + index, normalizedNames.get(index)));

    return namedParameterJdbcTemplate
        .query(
            "INSERT INTO tags (name) VALUES " + values + " RETURNING id, name",
            params,
            TAG_ENTITY_ROW_MAPPER)
        .stream()
        .map(TagEntity::toDomain)
        .toList();
  }

  private List<String> normalizeNames(List<String> names) {
    return names.stream().map(name -> new TagName(name).value()).distinct().toList();
  }
}
