package com.everrefine.elms.infrastructure.entity.tag;

import com.everrefine.elms.domain.model.lesson.LessonTag;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** タグのエンティティ。 */
@Table("tags")
public record TagEntity(
    @Id UUID id,
    String name
) {

  /**
   * ドメインモデルからエンティティを生成する。
   *
   * @param tag タグのドメインモデル
   * @return エンティティ
   */
  public static TagEntity from(LessonTag tag) {
    return new TagEntity(
        tag.id(),
        tag.name()
    );
  }

  /**
   * 新規作成用のエンティティを生成する。
   *
   * @param name タグ名
   * @return 新規作成用のエンティティ
   */
  public static TagEntity create(String name) {
    return new TagEntity(null, name);
  }

  /**
   * ドメインモデルに変換する。
   *
   * @return タグのドメインモデル
   */
  public LessonTag toDomain() {
    return new LessonTag(
        id,
        name
    );
  }
}
