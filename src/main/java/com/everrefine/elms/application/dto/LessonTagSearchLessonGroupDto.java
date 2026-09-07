package com.everrefine.elms.application.dto;

import com.everrefine.elms.domain.model.lesson.LessonTagSearchLessonGroup;
import com.everrefine.elms.domain.model.tag.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** タグ検索結果のレッスングループDTO。 */
public record LessonTagSearchLessonGroupDto(
    @Schema(description = "レッスングループID", example = "22222222-2222-2222-2222-222222222222")
        UUID lessonGroupId,
    @Schema(description = "レッスングループの表示順", example = "1.0") BigDecimal lessonGroupOrder,
    @Schema(description = "レッスングループタイトル", example = "レッスングループA") String lessonGroupTitle,
    @Schema(description = "指定タグに紐づくレッスン一覧") List<LessonTagSearchLessonDto> lessons) {

  /**
   * 検索結果のレッスングループからDTOを生成する。
   *
   * @param lessonGroup 検索結果のレッスングループ
   * @param tagsByLessonId レッスンIDごとのタグ一覧
   * @return タグ検索結果のレッスングループDTO
   */
  public static LessonTagSearchLessonGroupDto from(
      LessonTagSearchLessonGroup lessonGroup, Map<UUID, List<Tag>> tagsByLessonId) {
    return new LessonTagSearchLessonGroupDto(
        lessonGroup.lessonGroupId(),
        lessonGroup.lessonGroupOrder(),
        lessonGroup.lessonGroupTitle(),
        lessonGroup.lessons().stream()
            .map(
                lesson ->
                    LessonTagSearchLessonDto.from(
                        lesson, tagsByLessonId.getOrDefault(lesson.lessonId(), List.of())))
            .toList());
  }
}
