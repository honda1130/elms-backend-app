package com.everrefine.elms.application.util;

/** CSV出力処理で共通利用するユーティリティ。 */
public final class CsvUtils {

  private CsvUtils() {}

  /**
   * CSV出力用に値をエスケープする。
   *
   * <p>値にカンマ、ダブルクォーテーション、改行があった場合は、値全体をダブルクォーテーションで囲み、値中のダブルクォーテーションは2つに置き換える。
   *
   * @param value エスケープ対象の文字列
   * @return エスケープ後の文字列（nullの場合は空文字）
   */
  public static String escape(String value) {
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
