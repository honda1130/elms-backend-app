package com.everrefine.elms.presentation.request;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LessonUpdateRequestTest {

  @Test
  void コマンド変換時のタグ名はリクエスト値のまま渡されること() {
    LessonUpdateRequest request =
        new LessonUpdateRequest("更新後タイトル", "更新後説明", null, List.of(new LessonTagRequest(" Java ")));

    assertEquals(List.of(" Java "), request.toCommand(UUID.randomUUID()).tags());
  }
}
