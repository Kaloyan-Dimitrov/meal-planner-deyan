DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM   pg_constraint
        WHERE  conname = 'fk_recipe'
           AND conrelid = 'recipe_ingredient'::regclass
    ) THEN
ALTER TABLE recipe_ingredient
    ADD CONSTRAINT fk_recipe
        FOREIGN KEY (recipe_id)
            REFERENCES recipe(id);
END IF;
END;
$$;