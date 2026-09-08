package com.everrefine.elms.domain.model.lesson;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** タグ検索でヒットしたレッスンを含むレッスングループを表す読み取りモデル。 */
public record LessonTagSearchLessonGroup(
    UUID lessonGroupId,
    BigDecimal lessonGroupOrder,
    String lessonGroupTitle,
    List<LessonTagSearchLesson> lessons) {}
