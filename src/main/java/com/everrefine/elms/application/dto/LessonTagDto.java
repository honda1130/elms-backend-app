package com.everrefine.elms.application.dto;

import com.everrefine.elms.domain.model.lesson.LessonTag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** レッスンタグのDTO。 */
public record LessonTagDto(
    @Schema(
        description = "タグID",
        example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(
        description = "タグ名",
        example = "Java")
    String name) {

  /**
   * レッスンタグ情報からLessonTagDtoを生成する。
   *
   * @param tag レッスンタグ情報
   * @return レッスンタグのDTO
   */
  public static LessonTagDto from(LessonTag tag) {
    return new LessonTagDto(tag.id(), tag.name());
  }
}
