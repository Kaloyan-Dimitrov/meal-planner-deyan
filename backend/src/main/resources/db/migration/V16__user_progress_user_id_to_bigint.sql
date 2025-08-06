ALTER TABLE user_progress
    DROP CONSTRAINT user_progress_user_id_fkey;

ALTER TABLE user_progress
    ALTER COLUMN user_id TYPE BIGINT USING user_id::bigint;

ALTER TABLE user_progress
    ADD CONSTRAINT user_progress_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id);