package com.everrefine.elms.domain.model.lesson;

import com.everrefine.elms.domain.model.PagerForRequest;
import com.everrefine.elms.domain.model.tag.TagName;

/** タグに紐づくレッスンの検索条件の値オブジェクト。 */
public record LessonTagSearchCondition(TagName tagName, PagerForRequest pagerForRequest) {

  /**
   * タグ検索条件を作成する。
   *
   * @param tagName タグ名（前後の空白は取り除かれる）
   * @param pageNum ページ番号
   * @param pageSize 1ページ当たりの件数
   */
  public LessonTagSearchCondition(String tagName, int pageNum, int pageSize) {
    this(new TagName(tagName), new PagerForRequest(pageNum, pageSize));
  }

  /**
   * 検索に使用するタグ名を返す。
   *
   * @return 前後の空白を取り除いたタグ名
   */
  public String getTagNameValue() {
    return tagName.value();
  }

  /**
   * ページ番号を返す。
   *
   * @return ページ番号
   */
  public int getPageNum() {
    return pagerForRequest.pageNum();
  }

  /**
   * 1ページ当たりの件数を返す。
   *
   * @return 1ページ当たりの件数
   */
  public int getPageSize() {
    return pagerForRequest.pageSize();
  }

  /**
   * DBクエリ用のオフセット値を返す。
   *
   * @return オフセット値
   */
  public int getOffset() {
    return pagerForRequest.getOffset();
  }
}
