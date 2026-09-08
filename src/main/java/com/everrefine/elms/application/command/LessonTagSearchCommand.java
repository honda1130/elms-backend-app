package com.everrefine.elms.application.command;

import com.everrefine.elms.domain.model.lesson.LessonTagSearchCondition;

/** タグに紐づくレッスンの検索用のコマンド。 */
public record LessonTagSearchCommand(String tag, int pageNum, int pageSize) {

  /**
   * LessonTagSearchConditionに変換する。
   *
   * @return タグ検索条件
   */
  public LessonTagSearchCondition toSearchCondition() {
    return new LessonTagSearchCondition(tag, pageNum, pageSize);
  }
}
