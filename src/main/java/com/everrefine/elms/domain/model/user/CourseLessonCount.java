package com.everrefine.elms.domain.model.user;

/**
 * ユーザーのコース内の総レッスン数と完了レッスン数。
 *
 * @param allLessonCount コース内の総レッスン数
 * @param completedLessonCount ユーザーが完了したコース内のレッスン数
 */
public record CourseLessonCount(int allLessonCount, int completedLessonCount) {}
