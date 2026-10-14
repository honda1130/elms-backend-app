package com.everrefine.elms.application.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CsvExportUtilsTest {

  private static final String LINE_SEPARATOR = System.lineSeparator();

  /** 生成したCSVを文字列で返す。 */
  private String toCsvText(String[] header, List<String[]> rows) throws IOException {
    return new String(
        CsvExportUtils.toCsvResource(header, rows).getContentAsByteArray(), StandardCharsets.UTF_8);
  }

  /** 1行分のCSVを生成し、ヘッダー行（BOM含む）を除いたデータ行の文字列を返す。 */
  private String toDataLine(String... values) throws IOException {
    String csv = toCsvText(new String[] {"列"}, List.<String[]>of(values));
    return csv.substring(("\uFEFF列" + LINE_SEPARATOR).length());
  }

  @Nested
  class CSV生成 {

    @Test
    void 先頭にBOMが付きヘッダー行とデータ行が出力されること() throws IOException {
      String csv =
          toCsvText(
              new String[] {"ID", "名前"},
              List.of(new String[] {"1", "テスト1"}, new String[] {"2", "テスト2"}));

      assertEquals(
          "\uFEFFID,名前" + LINE_SEPARATOR + "1,テスト1" + LINE_SEPARATOR + "2,テスト2" + LINE_SEPARATOR,
          csv);
    }

    @Test
    void 行が0件の場合はヘッダー行のみ出力されること() throws IOException {
      String csv = toCsvText(new String[] {"ID", "名前"}, List.of());

      assertEquals("\uFEFFID,名前" + LINE_SEPARATOR, csv);
    }

    @Test
    void BOMがUTF8のバイト列で出力されること() throws IOException {
      byte[] bytes =
          CsvExportUtils.toCsvResource(new String[] {"ID"}, List.of()).getContentAsByteArray();

      assertEquals(
          List.of((byte) 0xEF, (byte) 0xBB, (byte) 0xBF),
          Arrays.asList(bytes[0], bytes[1], bytes[2]));
    }
  }

  @Nested
  class 値のエスケープ {

    @Test
    void nullの場合は空欄で出力されること() throws IOException {
      assertEquals("a,,b" + LINE_SEPARATOR, toDataLine("a", null, "b"));
    }

    @Test
    void 特殊文字を含まない値はそのまま出力されること() throws IOException {
      assertEquals("テスト title 1" + LINE_SEPARATOR, toDataLine("テスト title 1"));
    }

    @Test
    void カンマを含む値はダブルクォートで囲まれること() throws IOException {
      assertEquals("\"a,b\"" + LINE_SEPARATOR, toDataLine("a,b"));
    }

    @Test
    void ダブルクォートを含む値は囲まれ中のダブルクォートが2つになること() throws IOException {
      assertEquals("\"a\"\"b\"" + LINE_SEPARATOR, toDataLine("a\"b"));
    }

    @Test
    void LFを含む値はダブルクォートで囲まれること() throws IOException {
      assertEquals("\"a\nb\"" + LINE_SEPARATOR, toDataLine("a\nb"));
    }

    @Test
    void CRを含む値はダブルクォートで囲まれること() throws IOException {
      assertEquals("\"a\rb\"" + LINE_SEPARATOR, toDataLine("a\rb"));
    }

    @Test
    void 複数の特殊文字を含む値でも全体が1回だけ囲まれること() throws IOException {
      assertEquals("\"a,\"\"b\"\"\nc\"" + LINE_SEPARATOR, toDataLine("a,\"b\"\nc"));
    }
  }
}
