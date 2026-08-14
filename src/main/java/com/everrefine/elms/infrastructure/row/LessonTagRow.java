package com.everrefine.elms.infrastructure.row;

import com.everrefine.elms.domain.model.tag.Tag;
import java.util.UUID;

/** レッスンとタグをJOINしたセレクト結果の1行。 */
public record LessonTagRow(UUID lessonId, UUID tagId, String tagName) {

  /**
   * タグのドメインモデルに変換する。
   *
   * @return タグのドメインモデル
   */
  public Tag toTag() {
    return new Tag(tagId, tagName);
  }
}
