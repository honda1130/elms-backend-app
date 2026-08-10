package com.everrefine.elms.domain.repository;

import com.everrefine.elms.domain.model.lesson.LessonTag;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** タグのリポジトリインターフェース。 */
public interface TagRepository {

  /**
   * タグ名でタグを取得する。
   *
   * @param name タグ名
   * @return タグ（存在しない場合は空）
   */
  Optional<LessonTag> findByName(String name);

  /**
   * タグを作成する。
   *
   * @param name タグ名
   * @return 作成したタグ
   */
  LessonTag createTag(String name);

  /**
   * レッスンIDに紐づくタグ一覧を取得する。
   *
   * @param lessonId レッスンID
   * @return タグ一覧
   */
  List<LessonTag> findByLessonId(UUID lessonId);
}
