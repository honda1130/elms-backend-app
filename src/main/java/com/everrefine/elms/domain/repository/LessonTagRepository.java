package com.everrefine.elms.domain.repository;

import java.util.UUID;

/** レッスンとタグの紐付けリポジトリインターフェース。 */
public interface LessonTagRepository {

  /**
   * レッスンIDに紐づくタグ紐付けを削除する。
   *
   * @param lessonId レッスンID
   */
  void deleteByLessonId(UUID lessonId);

  /**
   * レッスンとタグの紐付けを作成する。
   *
   * @param lessonId レッスンID
   * @param tagId タグID
   */
  void createLessonTag(UUID lessonId, UUID tagId);
}
