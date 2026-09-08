package com.everrefine.elms.presentation.exception;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.everrefine.elms.testsupport.TestDataFactory;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * {@link GlobalExceptionHandler} の統合テストクラス。
 *
 * <p>コントローラーに宣言した {@code @ApiResponse} と、実際に返るHTTPステータスが一致することを検証する。 サービス層の例外型ではなく、例外ハンドラを通った後の
 * <b>実際のHTTPレスポンス</b> を確認する点が重要である。
 *
 * <p>{@code @ExceptionHandler(Exception.class)} のcatch-allは、具体的な例外を飲み込んで500にしてしまいやすい。
 * このテストはその退行を検出する。
 */
@ActiveProfiles("dev")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
public class GlobalExceptionHandlerTest {

  /** テストで使うDBを用意する。 */
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17").withReuse(true);

  /** 検証対象へのリクエスト送信に使用するMockMvc。 */
  @Autowired private MockMvc mockMvc;

  /** テストデータ作成ヘルパー。 */
  @Autowired private TestDataFactory testData;

  /** 検証で使い回す、存在しないID。 */
  private static final UUID MISSING_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private UUID adminId;

  @BeforeEach
  void authenticateAsAdmin() {
    SecurityContextHolder.clearContext();
    adminId = testData.createUser("admin@example.com", "pass", "管理 者", "adminuser", "ADMIN");
    authenticateAs(adminId, "ADMIN");
  }

  /** 指定したユーザーIDと権限で認証済みの状態にする。 */
  private void authenticateAs(UUID userId, String authority) {
    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                new User(userId.toString(), "password", authorities), null, authorities));
  }

  @Nested
  class リクエスト不正 {

    @Test
    void パスパラメータが不正なUUIDのときステータス400が返ること() throws Exception {
      mockMvc
          .perform(MockMvcRequestBuilders.get("/api/courses/not-a-uuid"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void タグ検索でタグ名が未指定のときステータス400が返ること() throws Exception {
      mockMvc
          .perform(MockMvcRequestBuilders.get("/api/lessons/search"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void タグ検索でページ番号が0以下のときステータス400が返ること() throws Exception {
      mockMvc
          .perform(
              MockMvcRequestBuilders.get("/api/lessons/search")
                  .param("tag", "Java")
                  .param("pageNum", "0"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void タグ検索で1ページ当たりの件数が0以下のときステータス400が返ること() throws Exception {
      mockMvc
          .perform(
              MockMvcRequestBuilders.get("/api/lessons/search")
                  .param("tag", "Java")
                  .param("pageSize", "0"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void リクエストボディのバリデーション違反のときステータス400が返ること() throws Exception {
      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/api/courses")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"title\":\"\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void 壊れたJSONボディのときステータス400が返ること() throws Exception {
      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/api/courses")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"title\": "))
          .andExpect(status().isBadRequest());
    }

    @Test
    void 必須のリクエストパートがないときステータス400が返ること() throws Exception {
      mockMvc
          .perform(MockMvcRequestBuilders.multipart("/api/files/upload"))
          .andExpect(status().isBadRequest());
    }

    @Test
    void 空ファイルアップロードのときErrorResponse付きでステータス400が返ること() throws Exception {
      mockMvc
          .perform(
              MockMvcRequestBuilders.multipart("/api/files/upload")
                  .file(new MockMultipartFile("file", "empty.png", "image/png", new byte[0])))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
          .andExpect(jsonPath("$.message").value("空のファイルです"));
    }

    @Test
    void 現在のパスワードが一致しないときステータス400が返ること() throws Exception {
      mockMvc
          .perform(
              MockMvcRequestBuilders.put("/api/users/password")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"currentPassword\":\"wrongPass\",\"newPassword\":\"newPass123\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
          .andExpect(jsonPath("$.message").value("現在のパスワードが一致しません"));
    }

    @Test
    void CSVに現在ログイン中ユーザーが含まれないときErrorResponse付きでステータス400が返ること() throws Exception {
      String csv = "権限,氏名,メールアドレス,ユーザー名\n管理者,山田 太郎,other@example.com,yamada\n";
      mockMvc
          .perform(
              MockMvcRequestBuilders.multipart("/api/users/import")
                  .file(
                      new MockMultipartFile(
                          "file", "users.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8))))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
          .andExpect(jsonPath("$.message").value("現在ログイン中のユーザーがCSVに含まれていません"));
    }

    @Test
    void パスワードリセット確定で無効なトークンのときErrorResponse付きでステータス400が返ること() throws Exception {
      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/api/password-reset/confirm")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"token\":\"missing-token\",\"newPassword\":\"newPass123\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
          .andExpect(jsonPath("$.message").value("無効なトークンです"));
    }

    @Test
    void ユーザー作成で不正なメールアドレスのときErrorResponse付きでステータス400が返ること() throws Exception {
      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/api/users")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {
                        "realName": "山田 太郎",
                        "userName": "yamada",
                        "emailAddress": "invalid-email",
                        "password": "password123",
                        "confirmPassword": "password123",
                        "thumbnailUrl": null,
                        "userRole": "GENERAL"
                      }
                      """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
          .andExpect(jsonPath("$.message").value("不正なメールアドレスです: invalid-email"));
    }

    /**
     * クライアント起因の文字数超過が500にならないことを検証する。
     *
     * <p>{@code NewsContent} は上限を超えると {@code InvalidValueException} を投げ、
     * これは想定外のシステムエラーとして500に変換される。リクエスト側で弾かないと クライアント起因の誤りが500として返ってしまうため、その退行を検出する。
     */
    @Test
    void お知らせ作成で本文が上限を超えるときステータス400が返ること() throws Exception {
      String content = "a".repeat(1_000_001);
      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/api/news")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"title\":\"テスト\",\"content\":\"" + content + "\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void お知らせ更新で本文が上限を超えるときステータス400が返ること() throws Exception {
      String content = "a".repeat(1_000_001);
      mockMvc
          .perform(
              MockMvcRequestBuilders.put("/api/news/{newsId}", MISSING_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"title\":\"テスト\",\"content\":\"" + content + "\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void レッスン並び順更新で前後IDがどちらもnullのときErrorResponse付きでステータス400が返ること() throws Exception {
      UUID courseId = testData.createCourse(new BigDecimal("987655"), "順序更新検証コース", "説明");
      UUID lessonGroupId =
          testData.createLessonGroup(courseId, new BigDecimal("1024"), "順序更新検証グループ");
      UUID lessonId =
          testData.createLesson(
              lessonGroupId, courseId, new BigDecimal("1024"), "順序更新検証レッスン", "本文", null);

      mockMvc
          .perform(
              MockMvcRequestBuilders.put(
                      "/api/courses/{courseId}/lesson-groups/{lessonGroupId}/lessons/{lessonId}/order",
                      courseId,
                      lessonGroupId,
                      lessonId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"precedingLessonId\":null,\"followingLessonId\":null}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
          .andExpect(jsonPath("$.message").value("前後のレッスンIDをどちらか一方は指定してください"));
    }
  }

  @Nested
  class 認証と認可 {

    @Test
    void 未認証のときステータス401が返ること() throws Exception {
      SecurityContextHolder.clearContext();
      mockMvc
          .perform(MockMvcRequestBuilders.get("/api/courses"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void トークン更新が匿名認証のときErrorResponse付きでステータス401が返ること() throws Exception {
      SecurityContextHolder.clearContext();
      mockMvc
          .perform(MockMvcRequestBuilders.get("/api/auth/refresh"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
          .andExpect(jsonPath("$.message").value("認証されていません"));
    }

    @Test
    void パスワード更新で認証ユーザーがDBに存在しないときステータス401が返ること() throws Exception {
      authenticateAs(MISSING_ID, "GENERAL");
      mockMvc
          .perform(
              MockMvcRequestBuilders.put("/api/users/password")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"currentPassword\":\"currentPass\",\"newPassword\":\"newPass123\"}"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
          .andExpect(jsonPath("$.message").value("認証されていません"));
    }

    @Test
    void 管理者権限が必要なAPIを一般ユーザーで呼ぶとステータス403が返ること() throws Exception {
      authenticateAs(adminId, "GENERAL");
      mockMvc
          .perform(MockMvcRequestBuilders.delete("/api/users/{userId}", MISSING_ID))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }
  }

  @Nested
  class リソース未検出 {

    @Test
    void 存在しないコースを取得するとステータス404が返ること() throws Exception {
      mockMvc
          .perform(MockMvcRequestBuilders.get("/api/courses/{courseId}", MISSING_ID))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void 存在しないレッスングループにレッスンを作成するとステータス404が返ること() throws Exception {
      UUID courseId = testData.createCourse(new BigDecimal("987654"), "検証コース", "説明");
      mockMvc
          .perform(
              MockMvcRequestBuilders.post(
                      "/api/courses/{courseId}/lesson-groups/{lessonGroupId}/lessons",
                      courseId,
                      MISSING_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"title\":\"レッスン\",\"content\":\"本文\",\"videoUrl\":null,\"tags\":[]}"))
          .andExpect(status().isNotFound());
    }

    @Test
    void 存在しないコースにレッスングループを作成するとステータス404が返ること() throws Exception {
      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/api/courses/{courseId}/lesson-groups", MISSING_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"title\":\"グループ\"}"))
          .andExpect(status().isNotFound());
    }

    @Test
    void ログイン中ユーザーが存在しない状態でCSV取込するとステータス404が返ること() throws Exception {
      authenticateAs(MISSING_ID, "ADMIN");
      String csv = "権限,氏名,メールアドレス,ユーザー名\n管理者,山田 太郎,a@example.com,yamada\n";
      mockMvc
          .perform(
              MockMvcRequestBuilders.multipart("/api/users/import")
                  .file(
                      new MockMultipartFile(
                          "file", "users.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8))))
          .andExpect(status().isNotFound());
    }

    @Test
    void 存在しないURLのときステータス404が返ること() throws Exception {
      mockMvc
          .perform(MockMvcRequestBuilders.get("/api/unknown-path"))
          .andExpect(status().isNotFound());
    }

    @Test
    void 削除は対象が存在しなくてもステータス204が返ること() throws Exception {
      mockMvc
          .perform(MockMvcRequestBuilders.delete("/api/courses/{courseId}", MISSING_ID))
          .andExpect(status().isNoContent());
      mockMvc
          .perform(MockMvcRequestBuilders.delete("/api/news/{newsId}", MISSING_ID))
          .andExpect(status().isNoContent());
      mockMvc
          .perform(MockMvcRequestBuilders.delete("/api/users/{userId}", MISSING_ID))
          .andExpect(status().isNoContent());
    }
  }

  @Nested
  class フレームワーク由来のエラー {

    @Test
    void サポートされないHTTPメソッドのときステータス405が返ること() throws Exception {
      mockMvc
          .perform(MockMvcRequestBuilders.patch("/api/courses"))
          .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void サポートされないContentTypeのときステータス415が返ること() throws Exception {
      mockMvc
          .perform(
              MockMvcRequestBuilders.post("/api/courses")
                  .contentType(MediaType.TEXT_PLAIN)
                  .content("hello"))
          .andExpect(status().isUnsupportedMediaType());
    }
  }
}
