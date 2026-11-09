package com.everrefine.elms.infrastructure.row;

import java.util.UUID;

/** コースごとの総レッスン数と、指定ユーザーの完了レッスン数の集約結果。 */
public record CourseLessonCountRow(
    UUID courseId, Integer allLessonCount, Integer completedLessonCount) {}
