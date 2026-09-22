-- 後続タスクのウェルカムメール送信を制御するフラグ。初期状態は無効とする。
INSERT INTO feature_flags (feature_flag_key, enabled)
VALUES ('welcome-mail', FALSE);
