CREATE TABLE tags
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE lesson_tags
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id UUID NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    tag_id    UUID NOT NULL REFERENCES tags (id) ON DELETE CASCADE,
    CONSTRAINT unique_lesson_tag UNIQUE (lesson_id, tag_id)
);
