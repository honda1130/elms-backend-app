package com.everrefine.elms.application.dto;

import com.everrefine.elms.domain.model.lesson.LessonTagSearchCourse;
import com.everrefine.elms.domain.model.tag.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** タグに紐づくレッスンの検索結果DTO。 */
public record LessonTagSearchResultDto(
    @Schema(description = "検索に使用したタグ名", example = "Java") String tag,
    @Schema(description = "検索結果のコース一覧") List<LessonTagSearchCourseDto> courses,
    @Schema(description = "現在のページ番号", example = "1") int pageNum,
    @Schema(description = "1ページ当たりの件数", example = "10") int pageSize,
    @Schema(description = "検索条件に一致するレッスンの総件数", example = "35") int totalSize) {

  /**
   * 検索結果からLessonTagSearchResultDtoを生成する。
   *
   * @param tag 検索に使用したタグ名
   * @param courses 検索結果のコース一覧
   * @param tagsByLessonId レッスンIDごとのタグ一覧
   * @param pageNum ページ番号
   * @param pageSize 1ページ当たりの件数
   * @param totalSize 検索条件に一致するレッスンの総件数
   * @return タグ検索結果DTO
   */
  public static LessonTagSearchResultDto from(
      String tag,
      List<LessonTagSearchCourse> courses,
      Map<UUID, List<Tag>> tagsByLessonId,
      int pageNum,
      int pageSize,
      int totalSize) {
    return new LessonTagSearchResultDto(
        tag,
        courses.stream()
            .map(course -> LessonTagSearchCourseDto.from(course, tagsByLessonId))
            .toList(),
        pageNum,
        pageSize,
        totalSize);
  }
}
