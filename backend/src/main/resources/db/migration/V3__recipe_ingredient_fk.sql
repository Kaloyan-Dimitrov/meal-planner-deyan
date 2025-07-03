ALTER TABLE recipe_ingredient
    ADD CONSTRAINT fk_recipe
        FOREIGN KEY (recipe_id) REFERENCES recipe(id);