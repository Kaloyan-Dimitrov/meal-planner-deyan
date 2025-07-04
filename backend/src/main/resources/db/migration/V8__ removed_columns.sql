ALTER TABLE ingredient
DROP COLUMN IF EXISTS category,
DROP COLUMN IF EXISTS kcal_per_100g;

DROP TABLE IF EXISTS instruction_step;

ALTER TABLE recipe
DROP COLUMN IF EXISTS kcal_per_serving,
DROP COLUMN IF EXISTS protein_g_per_serving,
DROP COLUMN IF EXISTS carb_g_per_serving,
DROP COLUMN IF EXISTS fat_g_per_serving;

ALTER TABLE recipe_ingredient
DROP COLUMN IF EXISTS quantity;