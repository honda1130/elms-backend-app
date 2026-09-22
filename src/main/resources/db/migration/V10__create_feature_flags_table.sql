CREATE TABLE feature_flags
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- フィーチャーフラグID
    feature_flag_key VARCHAR(100) NOT NULL UNIQUE,               -- フラグを識別するキー
    enabled          BOOLEAN      NOT NULL DEFAULT FALSE,        -- 機能が有効ならtrue
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 登録日時
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 更新日時
);
