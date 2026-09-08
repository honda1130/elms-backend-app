package com.everrefine.elms.presentation.controller;

import com.everrefine.elms.application.command.LessonTagSearchCommand;
import com.everrefine.elms.application.dto.LessonTagSearchResultDto;
import com.everrefine.elms.application.service.LessonApplicationService;
import com.everrefine.elms.presentation.request.LessonTagSearchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** レッスン検索のコントローラー。 */
@Tag(name = "レッスン検索")
@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonSearchController {

  private final LessonApplicationService lessonApplicationService;

  /**
   * 指定タグに紐づくレッスンを全コース横断で検索する。
   *
   * @param lessonTagSearchRequest タグ検索リクエスト（タグ名・ページ情報）
   * @return コース・レッスングループの階層でまとめた検索結果
   */
  @Operation(
      summary = "タグに紐づくレッスン検索",
      description = "指定したタグに紐づくレッスンを全コース横断で検索し、コース・レッスングループ・レッスンの階層で返します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "取得成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "500", description = "サーバーエラー")
  })
  @GetMapping("/search")
  public LessonTagSearchResultDto searchLessonsByTag(
      @Valid LessonTagSearchRequest lessonTagSearchRequest) {
    LessonTagSearchCommand lessonTagSearchCommand = lessonTagSearchRequest.toCommand();
    return lessonApplicationService.searchLessonsByTag(lessonTagSearchCommand);
  }
}
