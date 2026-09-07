package com.everrefine.elms.application.dto;

import com.everrefine.elms.domain.model.lesson.LessonTagSearchLesson;
import com.everrefine.elms.domain.model.tag.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** タグ検索結果のレッスンDTO。 */
public record LessonTagSearchLessonDto(
    @Schema(description = "レッスンID", example = "33333333-3333-3333-3333-333333333333") UUID lessonId,
    @Schema(description = "レッスンの表示順", example = "1.0") BigDecimal lessonOrder,
    @Schema(description = "レッスンタイトル", example = "レッスン1") String title,
    @Schema(
            description = "レッスンに紐づくタグ（検索条件のタグ以外も含む）",
            example = "[{\"id\":\"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa\",\"name\":\"Java\"}]")
        List<TagDto> tags) {

  /**
   * 検索結果のレッスンとタグ一覧からDTOを生成する。
   *
   * @param lesson 検索結果のレッスン
   * @param tags レッスンに紐づくタグ一覧
   * @return タグ検索結果のレッスンDTO
   */
  public static LessonTagSearchLessonDto from(LessonTagSearchLesson lesson, List<Tag> tags) {
    return new LessonTagSearchLessonDto(
        lesson.lessonId(),
        lesson.lessonOrder(),
        lesson.lessonTitle(),
        tags.stream().map(TagDto::from).toList());
  }
}
