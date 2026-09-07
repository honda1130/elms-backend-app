package com.everrefine.elms.presentation.request;

import com.everrefine.elms.application.command.LessonTagSearchCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** タグに紐づくレッスンの検索リクエスト。 */
public record LessonTagSearchRequest(
    @Schema(description = "検索対象のタグ名（必須・255文字以内）", example = "Java")
        @NotBlank(message = "タグ名は必須です")
        @Size(max = 255, message = "タグ名は255文字以内で入力してください")
        String tag,
    @Schema(description = "ページ番号（1始まり）", example = "1") @Positive(message = "ページ番号は1以上を指定してください") Integer page,
    @Schema(description = "1ページ当たりの件数", example = "10")
        @Positive(message = "1ページ当たりの件数は1以上を指定してください") Integer pageSize) {

  /**
   * Commandオブジェクトに変換する。ページ番号・件数が未指定の場合はデフォルト値（1 / 10）を適用する。
   *
   * @return タグ検索Command
   */
  public LessonTagSearchCommand toCommand() {
    return new LessonTagSearchCommand(
        tag, page == null ? 1 : page, pageSize == null ? 10 : pageSize);
  }
}
