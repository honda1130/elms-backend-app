package com.everrefine.elms.application.dto;

import com.everrefine.elms.domain.model.lesson.LessonTagSearchCourse;
import com.everrefine.elms.domain.model.tag.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** タグ検索結果のコースDTO。 */
public record LessonTagSearchCourseDto(
    @Schema(description = "コースID", example = "11111111-1111-1111-1111-111111111111") UUID courseId,
    @Schema(description = "コースの表示順", example = "1.0") BigDecimal courseOrder,
    @Schema(description = "コースタイトル", example = "コースA") String courseTitle,
    @Schema(description = "指定タグに紐づくレッスンを含むレッスングループ一覧")
        List<LessonTagSearchLessonGroupDto> lessonGroups) {

  /**
   * 検索結果のコースからDTOを生成する。
   *
   * @param course 検索結果のコース
   * @param tagsByLessonId レッスンIDごとのタグ一覧
   * @return タグ検索結果のコースDTO
   */
  public static LessonTagSearchCourseDto from(
      LessonTagSearchCourse course, Map<UUID, List<Tag>> tagsByLessonId) {
    return new LessonTagSearchCourseDto(
        course.courseId(),
        course.courseOrder(),
        course.courseTitle(),
        course.lessonGroups().stream()
            .map(lessonGroup -> LessonTagSearchLessonGroupDto.from(lessonGroup, tagsByLessonId))
            .toList());
  }
}
