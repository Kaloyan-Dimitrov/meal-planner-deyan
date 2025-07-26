ALTER TABLE achievement
    ADD COLUMN target INT NOT NULL DEFAULT 1;

-- === 2. Seed target values for existing rows ==============================
UPDATE achievement SET target = 1  WHERE id = 1;   -- 1-day streak
UPDATE achievement SET target = 7  WHERE id = 2;   -- 7-day streak
UPDATE achievement SET target = 30 WHERE id = 3;   -- 30-day streak
UPDATE achievement SET target = 10 WHERE id = 4;   -- 10 logs
UPDATE achievement SET target = 20 WHERE id = 5;   -- 20 logs
UPDATE achievement SET target = 30 WHERE id = 6;   -- 30 logs

-- === 3. Add progress to user_achievement ==================================
ALTER TABLE user_achievement
    ADD COLUMN progress INT NOT NULL DEFAULT 0;