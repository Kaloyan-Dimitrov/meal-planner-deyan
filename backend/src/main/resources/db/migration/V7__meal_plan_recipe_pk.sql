-- drop the old PK
ALTER TABLE meal_plan_recipe
DROP CONSTRAINT meal_plan_recipe_pkey;


ALTER TABLE meal_plan_recipe
    ADD CONSTRAINT meal_plan_recipe_pkey
        PRIMARY KEY (meal_plan_id, day_index, meal_slot);