package com.everrefine.elms.domain.model.lesson;

import java.math.BigDecimal;
import java.util.UUID;

/** タグ検索でヒットしたレッスンを表す読み取りモデル。 */
public record LessonTagSearchLesson(UUID lessonId, BigDecimal lessonOrder, String lessonTitle) {}
