-- 外部キー列のインデックスを追加（ON DELETE CASCADE の連鎖削除とタグ検索のJOINで全件走査になるのを防ぐ）
CREATE INDEX idx_user_lessons_lesson_id ON user_lessons (lesson_id);
CREATE INDEX idx_lessons_course_id ON lessons (course_id);
CREATE INDEX idx_user_login_histories_user_id ON user_login_histories (user_id);
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens (user_id);
CREATE INDEX idx_lesson_tags_tag_id ON lesson_tags (tag_id);