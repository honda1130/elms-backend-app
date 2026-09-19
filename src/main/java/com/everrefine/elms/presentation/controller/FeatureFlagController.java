package com.everrefine.elms.presentation.controller;

import com.everrefine.elms.application.dto.FeatureFlagDto;
import com.everrefine.elms.application.service.FeatureFlagApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** フィーチャーフラグに関するコントローラー。 */
@Tag(name = "フィーチャーフラグ")
@RestController
@RequestMapping("/api/feature-flags")
@RequiredArgsConstructor
public class FeatureFlagController {

  private final FeatureFlagApplicationService featureFlagApplicationService;

  /**
   * キーを指定してフィーチャーフラグの状態を取得する。
   *
   * <p>未登録のキーは「無効」として {@code 200 OK} で返す。フラグが未作成の状態でも呼び出し側が分岐できるようにするため、 {@code 404} は返さない。
   *
   * @param featureFlagKey 取得対象のフィーチャーフラグのキー
   * @return フィーチャーフラグの状態
   */
  @Operation(summary = "フィーチャーフラグ取得", description = "指定したキーのフィーチャーフラグが有効か無効かを返します。未登録のキーは無効として返します")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "取得成功"),
    @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
    @ApiResponse(responseCode = "401", description = "認証されていません"),
    @ApiResponse(responseCode = "403", description = "管理者権限が必要です"),
    @ApiResponse(responseCode = "500", description = "サーバーエラー")
  })
  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping("/{featureFlagKey}")
  public FeatureFlagDto getFeatureFlag(
      @PathVariable @NotBlank @Size(max = 100) String featureFlagKey) {
    return featureFlagApplicationService.getFeatureFlag(featureFlagKey);
  }
}
