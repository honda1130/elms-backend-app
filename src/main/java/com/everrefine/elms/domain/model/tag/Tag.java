package com.everrefine.elms.domain.model.tag;

import java.util.UUID;

/** タグのドメインモデル。 */
public record Tag(UUID id, TagName tagName) {

  public Tag(UUID id, String name) {
    this(id, new TagName(name));
  }

  /**
   * タグ名を取得する。
   *
   * @return タグ名
   */
  public String name() {
    return tagName.value();
  }
}
