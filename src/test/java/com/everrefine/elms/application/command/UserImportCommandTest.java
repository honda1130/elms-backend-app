package com.everrefine.elms.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.everrefine.elms.application.exception.BadRequestException;
import com.everrefine.elms.domain.model.user.UserRole;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/** {@link UserImportCommand} の単体テスト。 */
class UserImportCommandTest {

  private static final String HEADER = "権限,氏名,メールアドレス,ユーザー名";

  private static MultipartFile csvFile(String filename, String content) {
    return new MockMultipartFile(
        "file", filename, "text/csv", content.getBytes(StandardCharsets.UTF_8));
  }

  @Nested
  class CSV読み込み {
    @Test
    void BOM付きCSVとクォート付きカンマを読み込めること() {
      String csv =
          "\uFEFF" + String.join("\n", HEADER, "一般,\"テスト, 太郎\",test@example.com,\"user,name\"");
      MultipartFile file = csvFile("users.csv", csv);

      UserImportCommand command = UserImportCommand.from(file, UUID.randomUUID());

      assertEquals(1, command.getImportCount());
      UserImportRowCommand row = command.rows().get(0);
      assertEquals(UserRole.GENERAL, row.userRole());
      assertEquals("テスト, 太郎", row.realName());
      assertEquals("test@example.com", row.emailAddress());
      assertEquals("user,name", row.userName());
    }

    @Test
    void ヘッダのみCSVはCommand生成上は許容されること() {
      MultipartFile file = csvFile("users.csv", HEADER);

      UserImportCommand command = UserImportCommand.from(file, UUID.randomUUID());

      assertEquals(0, command.getImportCount());
    }
  }

  @Nested
  class CSVバリデーション {
    @Test
    void CSVファイル形式が不正な場合BadRequestExceptionが投げられること() {
      MultipartFile file = csvFile("users.txt", "test content");

      BadRequestException exception =
          assertThrows(
              BadRequestException.class, () -> UserImportCommand.from(file, UUID.randomUUID()));

      assertEquals("CSVファイル形式が不正です", exception.getMessage());
    }

    @Test
    void ヘッダ不正時はmessageが維持されたBadRequestExceptionが投げられること() {
      String csv = String.join("\n", "不正ヘッダ,氏名,メールアドレス,ユーザー名", "一般,山田太郎,t@example.com,taro");
      MultipartFile file = csvFile("users.csv", csv);

      BadRequestException exception =
          assertThrows(
              BadRequestException.class, () -> UserImportCommand.from(file, UUID.randomUUID()));

      assertEquals("CSVヘッダが不正です", exception.getMessage());
    }

    @Test
    void 空行を挟んでも物理行番号を示すBadRequestExceptionが投げられること() {
      String csv = String.join("\n", HEADER, "", "一般,,t@example.com,taro");
      MultipartFile file = csvFile("users.csv", csv);

      BadRequestException exception =
          assertThrows(
              BadRequestException.class, () -> UserImportCommand.from(file, UUID.randomUUID()));

      assertEquals("行3: 必須項目が入力されていません", exception.getMessage());
    }

    @Test
    void 未クローズクォートは列数不正のBadRequestExceptionが投げられること() {
      String csv = String.join("\n", HEADER, "一般,\"山田太郎,t@example.com,taro");
      MultipartFile file = csvFile("users.csv", csv);

      BadRequestException exception =
          assertThrows(
              BadRequestException.class, () -> UserImportCommand.from(file, UUID.randomUUID()));

      assertEquals("行2: 列数が不正です", exception.getMessage());
    }
  }
}
