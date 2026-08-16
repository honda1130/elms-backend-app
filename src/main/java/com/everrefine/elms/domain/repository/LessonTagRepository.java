package com.everrefine.elms.domain.repository;

import com.everrefine.elms.domain.model.tag.Tag;
import java.util.List;
import java.util.Map;
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
   * レッスンIDに紐づくタグ一覧を取得する。
   *
   * @param lessonId レッスンID
   * @return タグ一覧
   */
  List<Tag> findTagsByLessonId(UUID lessonId);

  /**
   * 複数のレッスンIDに紐づくタグ一覧を、レッスンIDごとに取得する。
   *
   * @param lessonIds レッスンID一覧
   * @return レッスンIDごとのタグ一覧
   */
  Map<UUID, List<Tag>> findTagsByLessonIdIn(List<UUID> lessonIds);

  /**
   * レッスンとタグの紐付けを一括作成する。
   *
   * @param lessonId レッスンID
   * @param tagIds タグID一覧
   */
  void createLessonTags(UUID lessonId, List<UUID> tagIds);
}
