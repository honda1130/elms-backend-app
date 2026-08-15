package com.everrefine.elms.domain.model.tag;

/** タグ名の値オブジェクト。 */
public record TagName(String value) {

  public TagName {
    value = value == null ? null : value.trim();
  }
}
