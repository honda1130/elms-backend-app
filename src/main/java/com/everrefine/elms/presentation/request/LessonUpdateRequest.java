package com.everrefine.elms.presentation.request;

import com.everrefine.elms.application.command.LessonUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/** レッスン更新リクエスト。 */
public record LessonUpdateRequest(
    @Schema(description = "レッスンタイトル（必須・255文字以内）", example = "変数とデータ型")
        @NotBlank(message = "レッスンタイトルは必須です")
        @Size(max = 255, message = "レッスンタイトルは255文字以内で入力してください")
        String title,
    @Schema(description = "レッスン本文（Markdown対応・1000000文字以内）", example = "## 変数とは\n変数はデータを格納する箱です。")
        @Size(max = 1_000_000, message = "レッスン本文は1000000文字以内で入力してください")
        String content,
    @Schema(description = "動画URL（2048文字以内）", example = "https://example.com/videos/lesson1.mp4")
        @Size(max = 2048, message = "動画URLは2048文字以内で入力してください")
        String videoUrl,
    @Schema(
            description = "レッスンタグ（必須・最大50個・空配列可。各タグ名は255文字以内）",
            example = "[{\"name\":\"Java\"}, {\"name\":\"Spring\"}]")
        @NotNull(message = "タグは必須です") @Size(max = 50, message = "登録できるタグの数は50個以内です")
        List<@NotNull(message = "タグ名は必須です") @Valid LessonTagRequest> tags) {

  /**
   * Commandオブジェクトに変換する。
   *
   * @param lessonId レッスンID
   * @return レッスン更新Command
   */
  public LessonUpdateCommand toCommand(UUID lessonId) {
    return new LessonUpdateCommand(
        lessonId, title, content, videoUrl, tags.stream().map(LessonTagRequest::name).toList());
  }
}
