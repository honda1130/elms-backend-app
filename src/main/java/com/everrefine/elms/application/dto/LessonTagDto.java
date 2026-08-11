package com.everrefine.elms.application.dto;

import com.everrefine.elms.domain.model.tag.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** レッスンタグのDTO。 */
public record LessonTagDto(
    @Schema(description = "タグID", example = "550e8400-e29b-41d4-a716-446655440000") UUID id,
    @Schema(description = "タグ名", example = "Java") String name) {

  /**
   * タグ情報からLessonTagDtoを生成する。
   *
   * @param tag タグ情報
   * @return レッスンタグのDTO
   */
  public static LessonTagDto from(Tag tag) {
    return new LessonTagDto(tag.id(), tag.name());
  }
}
