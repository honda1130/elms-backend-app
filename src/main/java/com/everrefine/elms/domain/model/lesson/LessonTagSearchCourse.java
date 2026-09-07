package com.everrefine.elms.domain.model.lesson;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** タグ検索でヒットしたレッスンを含むコースを表す読み取りモデル。 */
public record LessonTagSearchCourse(
    UUID courseId,
    BigDecimal courseOrder,
    String courseTitle,
    List<LessonTagSearchLessonGroup> lessonGroups) {}
