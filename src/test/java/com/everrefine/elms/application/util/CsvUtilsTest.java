package com.everrefine.elms.application.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CsvUtilsTest {

  @Nested
  class CSV値のエスケープ {

    @Test
    void nullの場合は空文字が返ること() {
      assertEquals("", CsvUtils.escape(null));
    }

    @Test
    void 空文字の場合は空文字が返ること() {
      assertEquals("", CsvUtils.escape(""));
    }

    @Test
    void 特殊文字を含まない値はそのまま返ること() {
      assertEquals("テスト title 1", CsvUtils.escape("テスト title 1"));
    }

    @Test
    void カンマを含む値はダブルクォートで囲まれること() {
      assertEquals("\"a,b\"", CsvUtils.escape("a,b"));
    }

    @Test
    void ダブルクォートを含む値は囲まれ中のダブルクォートが2つになること() {
      assertEquals("\"a\"\"b\"", CsvUtils.escape("a\"b"));
    }

    @Test
    void LFを含む値はダブルクォートで囲まれること() {
      assertEquals("\"a\nb\"", CsvUtils.escape("a\nb"));
    }

    @Test
    void CRを含む値はダブルクォートで囲まれること() {
      assertEquals("\"a\rb\"", CsvUtils.escape("a\rb"));
    }

    @Test
    void 複数の特殊文字を含む値でも全体が1回だけ囲まれること() {
      assertEquals("\"a,\"\"b\"\"\nc\"", CsvUtils.escape("a,\"b\"\nc"));
    }
  }
}
