package com.everrefine.elms.domain.repository;

import com.everrefine.elms.domain.model.tag.Tag;
import com.everrefine.elms.domain.model.tag.TagName;
import java.util.List;

/** タグのリポジトリインターフェース。 */
public interface TagRepository {

  /**
   * タグ名一覧でタグを取得する。
   *
   * @param tagNames タグ名一覧
   * @return タグ一覧
   */
  List<Tag> findByNameIn(List<TagName> tagNames);

  /**
   * タグを一括作成する。
   *
   * @param tagNames タグ名一覧
   * @return 作成したタグ一覧
   */
  List<Tag> createTags(List<TagName> tagNames);
}
