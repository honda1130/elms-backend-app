package com.everrefine.elms.application.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

/** CSV出力処理で共通利用するユーティリティ。 */
public final class CsvExportUtils {

  private CsvExportUtils() {}

  /**
   * ヘッダーと各行の値から、UTF-8（BOM付き）のCSVを生成する。
   *
   * <p>各行の値はエスケープしてから出力する。行が0件の場合は、ヘッダー行のみのCSVになる。
   *
   * @param header ヘッダー行の値
   * @param rows 各行の値（エスケープ前）
   * @return CSVリソース
   */
  public static Resource toCsvResource(String[] header, List<String[]> rows) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (PrintWriter writer =
        new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
      // Excelで開いたときの文字化けを防ぐ
      writer.print('\uFEFF');
      writer.println(String.join(",", header));
      for (String[] row : rows) {
        writer.println(String.join(",", Arrays.stream(row).map(CsvExportUtils::escape).toList()));
      }
    }
    return new ByteArrayResource(baos.toByteArray());
  }

  /**
   * CSV出力用に値をエスケープする。
   *
   * <p>値にカンマ、ダブルクォーテーション、改行があった場合は、値全体をダブルクォーテーションで囲み、値中のダブルクォーテーションは2つに置き換える。
   *
   * @param value エスケープ対象の文字列
   * @return エスケープ後の文字列（nullの場合は空文字）
   */
  private static String escape(String value) {
    if (value == null) {
      return "";
    }
    if (value.contains(",")
        || value.contains("\"")
        || value.contains("\n")
        || value.contains("\r")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }
}
