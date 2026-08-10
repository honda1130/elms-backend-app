package com.everrefine.elms.application.dto;

import com.everrefine.elms.domain.model.lesson.Lesson;
import com.everrefine.elms.domain.model.lesson.LessonGroupWithLessons;
import com.everrefine.elms.domain.model.lesson.LessonInGroup;
import com.everrefine.elms.domain.model.lesson.LessonTag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** レッスンのDTO。 */
public record LessonDto(
    @Schema(description = "レッスンID", example = "1") UUID id,
    @Schema(description = "レッスングループID", example = "2") UUID lessonGroupId,
    @Schema(description = "コースID", example = "3") UUID courseId,
    @Schema(description = "レッスンの表示順", example = "1.0") BigDecimal lessonOrder,
    @Schema(description = "レッスンタイトル", example = "変数とデータ型") String title,
    @Schema(description = "レッスンタグ", example = "[{\"id\":\"550e8400-e29b-41d4-a716-446655440000\",\"name\":\"Java\"}]")
        List<LessonTagDto> tags,
    @Schema(description = "レッスン本文（Markdown対応）", example = "## 変数とは\n変数はデータを格納する箱です。")
        String content,
    @Schema(description = "動画URL", example = "https://example.com/videos/lesson1.mp4")
        String videoUrl,
    @Schema(description = "登録日時", example = "2024-01-01T09:00:00") LocalDateTime createdAt,
    @Schema(description = "更新日時", example = "2024-06-01T10:30:00") LocalDateTime updatedAt) {

  /**
   * LessonエンティティからLessonDtoを生成する。
   *
   * @param lesson レッスンエンティティ
   * @return レッスンDTO
   */
  public static LessonDto from(Lesson lesson) {
    return new LessonDto(
        lesson.id(),
        lesson.lessonGroupId(),
        lesson.courseId(),
        lesson.lessonOrder().value(),
        lesson.title().value(),
        List.of(),
        lesson.content() != null ? lesson.content().value() : null,
        lesson.videoUrl() != null ? lesson.videoUrl().value() : null,
        lesson.createdAt(),
        lesson.updatedAt());
  }

  /**
   * レッスングループと配下レッスンの読み取りモデルからLessonDtoを生成する。
   *
   * @param group 所属するレッスングループの読み取りモデル
   * @param lesson レッスングループ配下のレッスン読み取りモデル
   * @return レッスンDTO
   */
  public static LessonDto from(LessonGroupWithLessons group, LessonInGroup lesson) {
    return new LessonDto(
        lesson.id(),
        group.id(),
        group.courseId(),
        lesson.lessonOrder(),
        lesson.title(),
        List.of(),
        lesson.content(),
        lesson.videoUrl(),
        lesson.createdAt(),
        lesson.updatedAt());
  }

  /**
   * レッスンエンティティとレッスンタグ情報からLessonDtoを生成する。
   *
   * @param lesson レッスンエンティティ
   * @param tags レッスンタグ情報
   * @return レッスンDTO
   */
  public static LessonDto from(Lesson lesson, List<LessonTag> tags) {
    return new LessonDto(
        lesson.id(),
        lesson.lessonGroupId(),
        lesson.courseId(),
        lesson.lessonOrder().value(),
        lesson.title().value(),
        tags.stream().map(LessonTagDto::from).toList(),
        lesson.content() != null ? lesson.content().value() : null,
        lesson.videoUrl() != null ? lesson.videoUrl().value() : null,
        lesson.createdAt(),
        lesson.updatedAt());
  }
}
