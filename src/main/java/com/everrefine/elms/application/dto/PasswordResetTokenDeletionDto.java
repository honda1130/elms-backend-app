package com.everrefine.elms.application.dto;

/**
 * 期限切れパスワードリセットトークンの削除結果DTO。
 *
 * @param deletedCount 削除件数
 */
public record PasswordResetTokenDeletionDto(int deletedCount) {}
