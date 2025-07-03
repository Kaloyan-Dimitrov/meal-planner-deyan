ALTER TABLE meal_plan_recipe
    ADD CONSTRAINT fk_mpr_recipe
        FOREIGN KEY (recipe_id) REFERENCES recipe(id);