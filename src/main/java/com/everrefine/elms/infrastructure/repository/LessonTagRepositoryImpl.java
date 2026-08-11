package com.everrefine.elms.infrastructure.repository;

import com.everrefine.elms.domain.repository.LessonTagRepository;
import com.everrefine.elms.infrastructure.dao.LessonTagDao;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** {@link LessonTagRepository} の実装。 */
@Repository
@AllArgsConstructor
public class LessonTagRepositoryImpl implements LessonTagRepository {

  private final LessonTagDao lessonTagDao;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

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

    String values =
        IntStream.range(0, distinctTagIds.size())
            .mapToObj(index -> "(:lessonId, :tagId" + index + ")")
            .collect(Collectors.joining(", "));
    MapSqlParameterSource params = new MapSqlParameterSource().addValue("lessonId", lessonId);
    IntStream.range(0, distinctTagIds.size())
        .forEach(index -> params.addValue("tagId" + index, distinctTagIds.get(index)));

    namedParameterJdbcTemplate.update(
        "INSERT INTO lesson_tags (lesson_id, tag_id) VALUES "
            + values
            + " ON CONFLICT (lesson_id, tag_id) DO NOTHING",
        params);
  }
}
