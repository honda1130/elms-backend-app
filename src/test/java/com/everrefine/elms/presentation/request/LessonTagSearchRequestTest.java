package com.everrefine.elms.presentation.request;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.everrefine.elms.application.command.LessonTagSearchCommand;
import org.junit.jupiter.api.Test;

class LessonTagSearchRequestTest {

  @Test
  void ページ番号と件数が未指定の場合にデフォルト値が設定されること() {
    LessonTagSearchRequest request = new LessonTagSearchRequest("Java", null, null);

    LessonTagSearchCommand command = request.toCommand();

    assertEquals(1, command.pageNum());
    assertEquals(10, command.pageSize());
  }

  @Test
  void コマンド変換時のタグ名はリクエスト値のまま渡されること() {
    LessonTagSearchRequest request = new LessonTagSearchRequest(" Java ", 2, 20);

    LessonTagSearchCommand command = request.toCommand();

    assertEquals(" Java ", command.tag());
    assertEquals(2, command.pageNum());
    assertEquals(20, command.pageSize());
  }
}
