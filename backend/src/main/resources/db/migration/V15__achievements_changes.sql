INSERT INTO achievement (id, name, description, target) VALUES
    (1, '1-day streak', 'Successfully log your weight for 1 day', 1),
       (2, '7-day streak', 'Successfully log your weight for 1 week', 7),
       (3, '30-day streak', 'Successfully log your weight for 1 month', 30),
    (4, '10 logs', 'Add 10 weight entries', 10),
     (5, '20 logs', 'Add 20 weight entries', 20),
     (6, '30 logs', 'Add 30 weight entries', 30);

ALTER TABLE recipe DROP COLUMN description;