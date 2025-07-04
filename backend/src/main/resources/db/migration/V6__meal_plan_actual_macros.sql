/* V6__meal_plan_actual_macros.sql – idempotent */

ALTER TABLE meal_plan
    ADD COLUMN IF NOT EXISTS actual_kcal       integer,
    ADD COLUMN IF NOT EXISTS actual_protein_g  integer,
    ADD COLUMN IF NOT EXISTS actual_carb_g     integer,
    ADD COLUMN IF NOT EXISTS actual_fat_g      integer;
