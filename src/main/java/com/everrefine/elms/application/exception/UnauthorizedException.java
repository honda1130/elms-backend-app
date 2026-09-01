package com.everrefine.elms.application.exception;

/** 認証が必要、または認証情報が無効な場合に発生する例外。 */
public class UnauthorizedException extends RuntimeException {

  /**
   * 認証エラーに関する例外を生成する。
   *
   * @param message エラーメッセージ
   */
  public UnauthorizedException(String message) {
    super(message);
  }

  /**
   * 認証エラーに関する例外を生成する。
   *
   * @param message エラーメッセージ
   * @param cause 原因例外
   */
  public UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }
}
