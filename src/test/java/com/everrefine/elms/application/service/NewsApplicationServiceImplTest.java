package com.everrefine.elms.application.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.everrefine.elms.application.dto.NewsDto;
import com.everrefine.elms.application.dto.NewsPageDto;
import com.everrefine.elms.presentation.request.NewsCreateRequest;
import com.everrefine.elms.presentation.request.NewsSearchRequest;
import com.everrefine.elms.presentation.request.NewsUpdateRequest;
import com.everrefine.elms.testsupport.TestDataFactory;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = WebEnvironment.NONE) // WebまわりのConfigurationはBean生成を無効にして高速化する。
@Testcontainers // DBはDockerコンテナを使用する。
@Transactional // 各テストメソッド終了時にテストデータをロールバックする。
public class NewsApplicationServiceImplTest {

  /** テストで使うDBを用意する。 */
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17").withReuse(true);

  /** テスト対象のサービスクラス。 */
  @Autowired private NewsApplicationServiceImpl newsApplicationService;

  /** テストデータ作成用のヘルパー。 */
  @Autowired private TestDataFactory testData;

  /** データ検証で使用するためのJdbcTemplate。 */
  @Autowired private JdbcTemplate jdbcTemplate;

  /** 作成日を指定してお知らせを登録する。並び順と日付範囲の検証に使うため created_at / updated_at を明示する。 */
  private void insertNewsWithDate(String title, String content, LocalDate createdDate) {
    LocalDateTime createdAt = createdDate.atTime(10, 0, 0);
    jdbcTemplate.update(
        """
            INSERT INTO news (title, content, created_at, updated_at)
            VALUES (?, ?, ?, ?)
            """,
        title,
        content,
        createdAt,
        createdAt);
  }

  @Nested
  class お知らせ作成 {
    @Test
    void お知らせを新規作成できること() {
      // Arrange
      NewsCreateRequest request = new NewsCreateRequest("テストタイトル", "テスト本文");
      newsApplicationService.createNews(request.toCommand());

      // 作成されたIDを取得する。
      UUID id =
          jdbcTemplate.queryForObject("SELECT id FROM news WHERE title = ?", UUID.class, "テストタイトル");
      assertNotNull(id);

      // Act
      NewsDto dto = newsApplicationService.findNewsById(id);

      // Assert
      assertEquals(id, dto.id());
      assertEquals("テストタイトル", dto.title());
      assertEquals("テスト本文", dto.content());
    }
  }

  @Nested
  class お知らせ取得 {
    @Test
    void タイトル部分一致と日付範囲でページング降順に取得できること() {
      // Arrange
      // データ5件投入（作成日をバラして後で日付範囲・並び順テストに使う）
      insertNewsWithDate("Java入門", "A", LocalDate.of(2025, 1, 1)); // 範囲外（後で除外される）
      insertNewsWithDate("Spring解説", "B", LocalDate.of(2025, 2, 1)); // タイトル不一致（後で除外）
      insertNewsWithDate("Java実践", "C", LocalDate.of(2025, 3, 1)); // 範囲内・一致
      insertNewsWithDate("旅行記", "D", LocalDate.of(2025, 4, 1)); // タイトル不一致（後で除外）
      insertNewsWithDate("Javaニュース", "E", LocalDate.of(2025, 5, 1)); // 範囲内・一致（最新）

      // Act:
      // タイトルに「Java」を含み、2025-02-01〜2025-05-31（両端含む*）の1ページ目・2件取得をリクエストする。
      NewsSearchRequest searchRequest =
          new NewsSearchRequest(1, 2, "Java", LocalDate.of(2025, 2, 1), LocalDate.of(2025, 5, 31));
      NewsPageDto page = newsApplicationService.findNews(searchRequest.toCommand());

      // Assert
      assertEquals(2, page.totalSize());
      assertEquals(1, page.pageNum());
      assertEquals(2, page.pageSize());

      var items = page.newsDtos();
      assertEquals(2, items.size());
      assertEquals("Javaニュース", items.get(0).title());
      assertEquals("Java実践", items.get(1).title());
    }
  }

  @Nested
  class お知らせ更新 {
    @Test
    void お知らせを更新できること() {
      // Arrange: まず1件作成
      NewsCreateRequest createRequest = new NewsCreateRequest("初期タイトル", "初期本文");
      newsApplicationService.createNews(createRequest.toCommand());
      UUID id =
          jdbcTemplate.queryForObject("SELECT id FROM news WHERE title = ?", UUID.class, "初期タイトル");
      assertNotNull(id);

      // （任意）更新前の updated_at を保持して後で更新されたことも検証
      Timestamp beforeUpdatedAt =
          jdbcTemplate.queryForObject(
              "SELECT updated_at FROM news WHERE id = ?", Timestamp.class, id);

      // Act: タイトルと本文を更新
      NewsUpdateRequest updateRequest = new NewsUpdateRequest(null, "更新後タイトル", "更新後本文");
      newsApplicationService.updateNews(updateRequest.toCommand(id));

      // Assert: DTO経由で内容が更新されていること
      NewsDto dto = newsApplicationService.findNewsById(id);
      assertEquals(id, dto.id());
      assertEquals("更新後タイトル", dto.title());
      assertEquals("更新後本文", dto.content());

      // DB上も行数が1件のままであること（上書き更新）
      Integer cnt =
          jdbcTemplate.queryForObject("SELECT COUNT(*) FROM news WHERE id = ?", Integer.class, id);
      assertEquals(1, cnt);

      // （任意）updated_at が更新されていること
      Timestamp afterUpdatedAt =
          jdbcTemplate.queryForObject(
              "SELECT updated_at FROM news WHERE id = ?", Timestamp.class, id);
      assertNotNull(beforeUpdatedAt);
      assertNotNull(afterUpdatedAt);
      assertTrue(
          afterUpdatedAt.after(beforeUpdatedAt),
          () ->
              "updated_at が更新前と同一か過去になっています: before="
                  + beforeUpdatedAt
                  + ", after="
                  + afterUpdatedAt);
    }

    @Test
    void 存在しないIDを更新するとResourceNotFoundExceptionが投げられること() {
      // Arrange
      UUID notExistsId = UUID.randomUUID();

      // Act & Assert
      NewsUpdateRequest request = new NewsUpdateRequest(null, "何か", "何か");
      assertThrows(
          RuntimeException.class,
          () -> newsApplicationService.updateNews(request.toCommand(notExistsId)));
    }
  }

  @Nested
  class お知らせCSV出力 {

    private static final String[] HEADER = {"お知らせID", "タイトル", "本文", "作成日時", "更新日時"};

    /** CSVを読み込み、先頭のBOMを除いた全行を返す。 */
    private List<String[]> readCsvRows(Resource csv) throws IOException, CsvValidationException {
      List<String[]> rows = new ArrayList<>();
      try (CSVReader csvReader =
          new CSVReader(new InputStreamReader(csv.getInputStream(), StandardCharsets.UTF_8))) {
        String[] row;
        while ((row = csvReader.readNext()) != null) {
          rows.add(row);
        }
      }
      if (!rows.isEmpty() && rows.get(0)[0].startsWith("\uFEFF")) {
        rows.get(0)[0] = rows.get(0)[0].substring(1);
      }
      return rows;
    }

    @Test
    void お知らせ全件が作成日時の降順でCSV出力されること() throws IOException, CsvValidationException {
      // Arrange
      jdbcTemplate.execute("DELETE FROM news");
      LocalDateTime dateTime1 = LocalDateTime.of(2026, 1, 1, 9, 0, 0);
      LocalDateTime dateTime2 = LocalDateTime.of(2026, 2, 1, 10, 30, 15);
      LocalDateTime dateTime3 = LocalDateTime.of(2026, 3, 1, 23, 59, 59);
      UUID oldId = testData.createNews("古いお知らせ", "古い本文", dateTime1);
      UUID middleId = testData.createNews("中間のお知らせ", "中間の本文", dateTime2);
      UUID newId = testData.createNews("新しいお知らせ", "新しい本文", dateTime3);
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

      // Act
      List<String[]> rows = readCsvRows(newsApplicationService.exportNewsCsv());

      // Assert
      assertEquals(4, rows.size());
      assertArrayEquals(HEADER, rows.get(0));
      assertArrayEquals(
          new String[] {
            String.valueOf(newId),
            "新しいお知らせ",
            "新しい本文",
            dateTime3.format(formatter),
            dateTime3.format(formatter)
          },
          rows.get(1));
      assertArrayEquals(
          new String[] {
            String.valueOf(middleId),
            "中間のお知らせ",
            "中間の本文",
            dateTime2.format(formatter),
            dateTime2.format(formatter)
          },
          rows.get(2));
      assertArrayEquals(
          new String[] {
            String.valueOf(oldId),
            "古いお知らせ",
            "古い本文",
            dateTime1.format(formatter),
            dateTime1.format(formatter)
          },
          rows.get(3));
    }

    @Test
    void 作成日時が同じお知らせはIDの昇順で出力されること() throws IOException, CsvValidationException {
      // Arrange
      jdbcTemplate.execute("DELETE FROM news");
      LocalDateTime sameDateTime = LocalDateTime.of(2026, 4, 1, 12, 0, 0);
      UUID id1 = testData.createNews("お知らせA", "本文A", sameDateTime);
      UUID id2 = testData.createNews("お知らせB", "本文B", sameDateTime);
      UUID id3 = testData.createNews("お知らせC", "本文C", sameDateTime);
      // DBのuuid型は符号なしのバイト順で並ぶ。JavaのUUID比較は符号付きのため、文字列（小文字16進）で並べて期待値にする。
      List<String> expectedIds =
          List.of(id1, id2, id3).stream().map(String::valueOf).sorted().toList();

      // Act
      List<String[]> rows = readCsvRows(newsApplicationService.exportNewsCsv());

      // Assert
      assertEquals(4, rows.size());
      assertEquals(expectedIds, rows.stream().skip(1).map(row -> row[0]).toList());
    }

    @Test
    void カンマとダブルクォートと改行を含む値がエスケープされて出力されること() throws IOException, CsvValidationException {
      // Arrange
      jdbcTemplate.execute("DELETE FROM news");
      LocalDateTime dateTime = LocalDateTime.of(2026, 5, 1, 8, 0, 0);
      UUID newsId = testData.createNews("タイトル,\"引用\"", "1行目\n2行目,\"3\"", dateTime);

      // Act
      Resource csv = newsApplicationService.exportNewsCsv();
      String csvText = new String(csv.getContentAsByteArray(), StandardCharsets.UTF_8);
      List<String[]> rows = readCsvRows(csv);

      // Assert
      // 生のCSV文字列がエスケープ規則どおりであること
      assertTrue(csvText.contains("\"タイトル,\"\"引用\"\"\""));
      assertTrue(csvText.contains("\"1行目\n2行目,\"\"3\"\"\""));
      // 読み戻した値が元の値と一致すること
      assertEquals(2, rows.size());
      assertEquals(String.valueOf(newsId), rows.get(1)[0]);
      assertEquals("タイトル,\"引用\"", rows.get(1)[1]);
      assertEquals("1行目\n2行目,\"3\"", rows.get(1)[2]);
    }

    @Test
    void お知らせが0件の場合はヘッダー行のみ出力されること() throws IOException, CsvValidationException {
      // Arrange
      jdbcTemplate.execute("DELETE FROM news");

      // Act
      List<String[]> rows = readCsvRows(newsApplicationService.exportNewsCsv());

      // Assert
      assertEquals(1, rows.size());
      assertArrayEquals(HEADER, rows.get(0));
    }

    @Test
    void 先頭にUTF8のBOMが付くこと() throws IOException {
      // Arrange
      jdbcTemplate.execute("DELETE FROM news");

      // Act
      byte[] bytes = newsApplicationService.exportNewsCsv().getContentAsByteArray();

      // Assert
      assertEquals((byte) 0xEF, bytes[0]);
      assertEquals((byte) 0xBB, bytes[1]);
      assertEquals((byte) 0xBF, bytes[2]);
    }

    @Test
    void 一覧のページサイズを超える件数でも全件出力されること() throws IOException, CsvValidationException {
      // Arrange
      jdbcTemplate.execute("DELETE FROM news");
      LocalDateTime baseDateTime = LocalDateTime.of(2026, 6, 1, 0, 0, 0);
      for (int i = 0; i < 35; i++) {
        testData.createNews("お知らせ" + i, "本文" + i, baseDateTime.plusMinutes(i));
      }

      // Act
      List<String[]> rows = readCsvRows(newsApplicationService.exportNewsCsv());

      // Assert
      assertEquals(36, rows.size());
      assertEquals("お知らせ34", rows.get(1)[1]);
      assertEquals("お知らせ0", rows.get(35)[1]);
    }
  }
}
