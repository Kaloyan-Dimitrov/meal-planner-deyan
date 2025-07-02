package com.deyan.mealplanner.service;

import com.deyan.mealplanner.dto.IngredientDTO;
import com.deyan.mealplanner.dto.NewIngredientDTO;
import com.deyan.mealplanner.jooq.tables.Ingredient;
import com.deyan.mealplanner.jooq.tables.records.IngredientRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.deyan.mealplanner.jooq.tables.Ingredient.INGREDIENT;
import static com.deyan.mealplanner.jooq.tables.Users.USERS;

@Service
public class IngredientService {
    private final DSLContext dsl;
    public IngredientService(DSLContext dsl) {
        this.dsl = dsl;
    }
    public List<IngredientDTO> getAllIngredients(){
        return dsl.selectFrom(INGREDIENT).fetch().map(this::toDto);
    }
    public IngredientDTO getIngredientById(Long id){
        return dsl.selectFrom(INGREDIENT)
                .where(INGREDIENT.ID.eq(id))
                .fetchOptional()
                .map(this::toDto)
                .orElseThrow(()->new IllegalArgumentException("Ingredient not found " + id));
    }
    public IngredientDTO createIngredient(NewIngredientDTO ingredientDTO){
        boolean exists = dsl.fetchExists(
                dsl.selectOne()
                        .from(INGREDIENT)
                        .where(INGREDIENT.NAME.eq(ingredientDTO.name())
                                .and(INGREDIENT.CATEGORY.eq(ingredientDTO.category())))
        );
        if (exists) {
            throw new IllegalArgumentException("Ingredient already exists: " + ingredientDTO.name());
        }

        // 1) plain INSERT — **no RETURNING**
        dsl.insertInto(INGREDIENT)
                .set(INGREDIENT.NAME,          ingredientDTO.name())
                .set(INGREDIENT.CATEGORY,      ingredientDTO.category())
                .set(INGREDIENT.KCAL_PER_100G, ingredientDTO.kcalPer100g())
                .execute();

        // 2) fetch the generated PK from the session sequence
        //    works because we're still on the same connection
        var id = dsl.select(INGREDIENT.ID)
                .from(INGREDIENT)
                .where(INGREDIENT.NAME.eq(ingredientDTO.name())) // or any unique field
                .fetchOne(INGREDIENT.ID);

        return new IngredientDTO(id, ingredientDTO.name(), ingredientDTO.category(), ingredientDTO.kcalPer100g());
    }

    public IngredientDTO update(Long id, IngredientDTO dto) {
        int updated = dsl.execute(
                """
                UPDATE ingredient
                   SET name           = ?,
                       category       = ?,
                       kcal_per_100g  = ?
                 WHERE id = ?
                """,
                dto.name(),
                dto.category(),
                dto.kcalPer100g(),
                id
        );

        if (updated == 0) {
            throw new IllegalArgumentException("Ingredient not found: " + id);
        }

        // re-read the row
        IngredientRecord rec = dsl.fetchOne(INGREDIENT, INGREDIENT.ID.eq(id));
        return new IngredientDTO(
                rec.getId(),
                rec.getName(),
                rec.getCategory(),
                rec.getKcalPer_100g()
        );
    }
    public void delete(Long id){
        int deleted = dsl.delete(INGREDIENT)
                .where(INGREDIENT.ID.eq(id))
                .execute();
        if(deleted==0){
            throw new IllegalArgumentException("Ingredient not found " + id);
        }
    }
    private IngredientDTO toDto(IngredientRecord ingredient) {
        return new IngredientDTO(ingredient.getId(),ingredient.getName()
                ,ingredient.getCategory(),ingredient.getKcalPer_100g());
    }
}
