package com.everrefine.elms.domain.repository;

import java.util.List;
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
   * レッスンとタグの紐付けを一括作成する。
   *
   * @param lessonId レッスンID
   * @param tagIds タグID一覧
   */
  void createLessonTags(UUID lessonId, List<UUID> tagIds);
}
