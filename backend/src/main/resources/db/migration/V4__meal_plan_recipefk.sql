DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM   pg_constraint
        WHERE  conname = 'fk_mpr_recipe'
          AND  conrelid = 'meal_plan_recipe'::regclass
    ) THEN
ALTER TABLE meal_plan_recipe
    ADD CONSTRAINT fk_mpr_recipe
        FOREIGN KEY (recipe_id)
            REFERENCES recipe(id);
END IF;
END;
$$;;