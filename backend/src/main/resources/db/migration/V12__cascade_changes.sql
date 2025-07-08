/* ========== USERS parent ========= */
-- Drop existing restrictive constraints
ALTER TABLE meal_plan          DROP CONSTRAINT meal_plan_user_id_fkey;
ALTER TABLE user_progress      DROP CONSTRAINT user_progress_user_id_fkey;
ALTER TABLE user_achievement   DROP CONSTRAINT user_achievement_user_id_fkey;

-- Re-create with cascade
ALTER TABLE meal_plan
    ADD CONSTRAINT meal_plan_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE user_progress
    ADD CONSTRAINT user_progress_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE user_achievement
    ADD CONSTRAINT user_achievement_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

/* ========== MEAL_PLAN parent ========= */
ALTER TABLE meal_plan_recipe   DROP CONSTRAINT meal_plan_recipe_meal_plan_id_fkey;
ALTER TABLE shopping_list      DROP CONSTRAINT shopping_list_meal_plan_id_fkey;

ALTER TABLE meal_plan_recipe
    ADD CONSTRAINT meal_plan_recipe_meal_plan_id_fkey
        FOREIGN KEY (meal_plan_id) REFERENCES meal_plan(id) ON DELETE CASCADE;

ALTER TABLE shopping_list
    ADD CONSTRAINT shopping_list_meal_plan_id_fkey
        FOREIGN KEY (meal_plan_id) REFERENCES meal_plan(id) ON DELETE CASCADE;

/* ========== RECIPE parent ========= */

ALTER TABLE recipe_ingredient  DROP CONSTRAINT recipe_ingredient_recipe_id_fkey;
ALTER TABLE meal_plan_recipe   DROP CONSTRAINT meal_plan_recipe_recipe_id_fkey;


ALTER TABLE recipe_ingredient
    ADD CONSTRAINT recipe_ingredient_recipe_id_fkey
        FOREIGN KEY (recipe_id) REFERENCES recipe(id) ON DELETE CASCADE;

ALTER TABLE meal_plan_recipe
    ADD CONSTRAINT meal_plan_recipe_recipe_id_fkey
        FOREIGN KEY (recipe_id) REFERENCES recipe(id) ON DELETE CASCADE;

/* ========== INGREDIENT parent ========= */
ALTER TABLE recipe_ingredient   DROP CONSTRAINT recipe_ingredient_ingredient_id_fkey;
ALTER TABLE shopping_list_item  DROP CONSTRAINT shopping_list_item_ingredient_id_fkey;

ALTER TABLE recipe_ingredient
    ADD CONSTRAINT recipe_ingredient_ingredient_id_fkey
        FOREIGN KEY (ingredient_id) REFERENCES ingredient(id) ON DELETE CASCADE;

ALTER TABLE shopping_list_item
    ADD CONSTRAINT shopping_list_item_ingredient_id_fkey
        FOREIGN KEY (ingredient_id) REFERENCES ingredient(id) ON DELETE CASCADE;

/* ========== SHOPPING_LIST parent ========= */
ALTER TABLE shopping_list_item  DROP CONSTRAINT shopping_list_item_shopping_list_id_fkey;

ALTER TABLE shopping_list_item
    ADD CONSTRAINT shopping_list_item_shopping_list_id_fkey
        FOREIGN KEY (shopping_list_id) REFERENCES shopping_list(id) ON DELETE CASCADE;

/* ========== ACHIEVEMENT parent (optional but handy) ========= */
ALTER TABLE user_achievement DROP CONSTRAINT user_achievement_achievement_id_fkey;
ALTER TABLE user_achievement
    ADD CONSTRAINT user_achievement_achievement_id_fkey
        FOREIGN KEY (achievement_id) REFERENCES achievement(id) ON DELETE CASCADE;
