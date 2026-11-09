package com.everrefine.elms.presentation;

/** 一覧APIのページング上限。 */
public final class PageLimits {

  /** 1ページ当たりの件数の上限。 */
  public static final int MAX_PAGE_SIZE = 100;

  /** ページ番号の上限。(pageNum - 1) * pageSize がintの最大値を超えない値とする。 */
  public static final int MAX_PAGE_NUM = Integer.MAX_VALUE / MAX_PAGE_SIZE + 1;

  /** ページ番号が上限を超えたときのメッセージ。 */
  public static final String MAX_PAGE_NUM_MESSAGE = "ページ番号は" + MAX_PAGE_NUM + "以下を指定してください";

  /** 1ページ当たりの件数が上限を超えたときのメッセージ。 */
  public static final String MAX_PAGE_SIZE_MESSAGE = "1ページ当たりの件数は" + MAX_PAGE_SIZE + "以下を指定してください";

  private PageLimits() {}
}
