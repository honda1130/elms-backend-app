package com.everrefine.elms.infrastructure.entity.tag;

import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** レッスンタグ紐付けのエンティティ。 */
@Table("lesson_tags")
public record LessonTagEntity(@Id UUID id, UUID lessonId, UUID tagId) {}
