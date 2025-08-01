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

    /**
     * Constructs the service with a JOOQ DSL context.
     *
     * @param dsl The injected {@link DSLContext} for database access.
     */
    public IngredientService(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * Retrieves all ingredients from the database.
     *
     * @return A list of {@link IngredientDTO} objects.
     */
    public List<IngredientDTO> getAllIngredients() {
        return dsl.selectFrom(INGREDIENT).fetch().map(this::toDto);
    }

    /**
     * Retrieves a single ingredient by its ID.
     *
     * @param id The ID of the ingredient.
     * @return The matching {@link IngredientDTO}.
     * @throws NotFoundException if no ingredient is found with the given ID.
     */
    public IngredientDTO getIngredientById(Long id) {
        return dsl.selectFrom(INGREDIENT)
                .where(INGREDIENT.ID.eq(id))
                .fetchOptional()
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Ingredient not found: " + id));
    }

    /**
     * Creates a new ingredient.
     *
     * @param ingredientDTO The ingredient creation request.
     * @return The created {@link IngredientDTO}.
     * @throws AlreadyExistsException if an ingredient with the same name already exists.
     */
    public IngredientDTO createIngredient(NewIngredientDTO ingredientDTO) {
        boolean exists = dsl.fetchExists(
                dsl.selectOne()
                        .from(INGREDIENT)
                        .where(INGREDIENT.NAME.eq(ingredientDTO.name())));

        if (exists) {
            throw new AlreadyExistsException("Ingredient already exists: " + ingredientDTO.name());
        }

        dsl.insertInto(INGREDIENT)
                .set(INGREDIENT.NAME, ingredientDTO.name())
                .execute();

        var id = dsl.select(INGREDIENT.ID)
                .from(INGREDIENT)
                .where(INGREDIENT.NAME.eq(ingredientDTO.name()))
                .fetchOne(INGREDIENT.ID);

        return new IngredientDTO(id, ingredientDTO.name());
    }

    /**
     * Updates the name of an existing ingredient.
     *
     * @param id  The ID of the ingredient to update.
     * @param dto The new data for the ingredient.
     * @return The updated {@link IngredientDTO}.
     * @throws NotFoundException if no ingredient exists with the given ID.
     */
    public IngredientDTO update(Long id, IngredientDTO dto) {
        int updated = dsl.execute(
                """
                UPDATE ingredient
                   SET name = ?
                 WHERE id = ?
                """,
                dto.name(),
                id
        );

        if (updated == 0) {
            throw new NotFoundException("Ingredient not found: " + id);
        }

        IngredientRecord rec = dsl.fetchOne(INGREDIENT, INGREDIENT.ID.eq(id));
        return new IngredientDTO(rec.getId(), rec.getName());
    }

    /**
     * Deletes an ingredient by ID.
     *
     * @param id The ID of the ingredient to delete.
     * @throws NotFoundException if the ingredient does not exist.
     */
    public void delete(Long id) {
        int deleted = dsl.delete(INGREDIENT)
                .where(INGREDIENT.ID.eq(id))
                .execute();

        if (deleted == 0) {
            throw new NotFoundException("Ingredient not found " + id);
        }
    }

    /**
     * Helper method to map a JOOQ {@link IngredientRecord} to a DTO.
     *
     * @param ingredient The database record.
     * @return The equivalent {@link IngredientDTO}.
     */
    private IngredientDTO toDto(IngredientRecord ingredient) {
        return new IngredientDTO(ingredient.getId(), ingredient.getName());
    }
}
