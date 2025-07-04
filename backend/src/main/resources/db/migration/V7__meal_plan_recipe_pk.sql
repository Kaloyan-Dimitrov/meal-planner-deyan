/* V7__meal_plan_recipe_pk.sql – switch to composite PK
   ----------------------------------------------------
   ▸ Removes any duplicate rows that would violate the new key
   ▸ Drops the old PK if it exists
   ▸ Adds the composite PK only if it’s still missing
   ▸ Safe to re-run (Flyway can happily validate / repair)
*/

DO $$
DECLARE
dup_rows BIGINT;
BEGIN
    ----------------------------------------------------------------------
    -- 1.  Clean duplicates (keep the first row for each PK combination)
    ----------------------------------------------------------------------
WITH ranked AS (
    SELECT ctid,
           ROW_NUMBER() OVER (
                   PARTITION BY meal_plan_id, day_index, meal_slot
                   ORDER BY ctid
               ) AS rn
    FROM   meal_plan_recipe
)
DELETE FROM meal_plan_recipe mpr
    USING  ranked r
WHERE  mpr.ctid = r.ctid
  AND  r.rn > 1;

GET DIAGNOSTICS dup_rows = ROW_COUNT;
RAISE NOTICE 'Removed % duplicate rows before adding composite PK', dup_rows;

    ----------------------------------------------------------------------
    -- 2.  Drop any existing primary-key constraint
    ----------------------------------------------------------------------
    IF EXISTS (
        SELECT 1
        FROM   pg_constraint
        WHERE  conname  = 'meal_plan_recipe_pkey'
          AND  conrelid = 'meal_plan_recipe'::regclass
    ) THEN
ALTER TABLE meal_plan_recipe
DROP CONSTRAINT meal_plan_recipe_pkey;
END IF;

    ----------------------------------------------------------------------
    -- 3.  Create the new composite PK (if it isn’t there already)
    ----------------------------------------------------------------------
    IF NOT EXISTS (
        SELECT 1
        FROM   pg_constraint
        WHERE  conname  = 'meal_plan_recipe_pkey'
          AND  conrelid = 'meal_plan_recipe'::regclass
    ) THEN
ALTER TABLE meal_plan_recipe
    ADD CONSTRAINT meal_plan_recipe_pkey
        PRIMARY KEY (meal_plan_id, day_index, meal_slot);
END IF;
END;
$$;
