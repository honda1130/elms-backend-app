package com.everrefine.elms.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** レッスンタグリクエスト。 */
public record LessonTagRequest(
    @Schema(description = "タグ名（必須・255文字以内）", example = "Java")
        @NotBlank(message = "タグ名は必須です")
        @Size(max = 255, message = "タグ名は255文字以内で入力してください")
        String name) {}
