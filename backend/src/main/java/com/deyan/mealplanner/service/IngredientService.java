package com.deyan.mealplanner.service;

import com.deyan.mealplanner.exceptions.AlreadyExistsException;
import com.deyan.mealplanner.dto.IngredientDTO;
import com.deyan.mealplanner.dto.NewIngredientDTO;
import com.deyan.mealplanner.exceptions.NotFoundException;
import com.deyan.mealplanner.jooq.tables.records.IngredientRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.deyan.mealplanner.jooq.tables.Ingredient.INGREDIENT;

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
                .orElseThrow(()->new NotFoundException("Ingredient not found: " + id));
    }
    public IngredientDTO createIngredient(NewIngredientDTO ingredientDTO){
        boolean exists = dsl.fetchExists(
                dsl.selectOne()
                        .from(INGREDIENT)
                        .where(INGREDIENT.NAME.eq(ingredientDTO.name())));
        if (exists) {
            throw new AlreadyExistsException("Ingredient already exists: " + ingredientDTO.name());
        }

        dsl.insertInto(INGREDIENT)
                .set(INGREDIENT.NAME,          ingredientDTO.name())
                .execute();

        var id = dsl.select(INGREDIENT.ID)
                .from(INGREDIENT)
                .where(INGREDIENT.NAME.eq(ingredientDTO.name())) // or any unique field
                .fetchOne(INGREDIENT.ID);

        return new IngredientDTO(id, ingredientDTO.name());
    }

    public IngredientDTO update(Long id, IngredientDTO dto) {
        int updated = dsl.execute(
                """
                UPDATE ingredient
                   SET name           = ?,
                 WHERE id = ?
                """,
                dto.name(),
                id
        );

        if (updated == 0) {
            throw new NotFoundException("Ingredient not found: " + id);
        }

        IngredientRecord rec = dsl.fetchOne(INGREDIENT, INGREDIENT.ID.eq(id));
        return new IngredientDTO(
                rec.getId(),
                rec.getName()
        );
    }
    public void delete(Long id){
        int deleted = dsl.delete(INGREDIENT)
                .where(INGREDIENT.ID.eq(id))
                .execute();
        if(deleted==0){
            throw new NotFoundException("Ingredient not found " + id);
        }
    }
    private IngredientDTO toDto(IngredientRecord ingredient) {
        return new IngredientDTO(ingredient.getId(),ingredient.getName());
    }
}
