package com.everrefine.elms.domain.repository;

import com.everrefine.elms.domain.model.tag.Tag;
import java.util.List;

/** タグのリポジトリインターフェース。 */
public interface TagRepository {

  /**
   * タグ名一覧でタグを取得する。
   *
   * @param names タグ名一覧
   * @return タグ一覧
   */
  List<Tag> findByNameIn(List<String> names);

  /**
   * タグを一括作成する。
   *
   * @param names タグ名一覧
   * @return 作成したタグ一覧
   */
  List<Tag> createTags(List<String> names);
}
