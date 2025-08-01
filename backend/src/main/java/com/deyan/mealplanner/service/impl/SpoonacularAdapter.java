package com.deyan.mealplanner.service.impl;

import com.deyan.mealplanner.dto.*;
import com.deyan.mealplanner.exceptions.ExternalApiQuotaException;
import com.deyan.mealplanner.service.interfaces.RecipeAPIAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!test")
public class SpoonacularAdapter implements RecipeAPIAdapter {

    private final WebClient web;

    @Value("${spoonacular.key}")
    private String apiKey;

    private static final String PLAN_ENDPOINT = "/mealplanner/generate";
    private static final String INFO_ENDPOINT =
            "/recipes/{id}/information?includeNutrition=true&apiKey={key}";

    /**
     * Generates a meal plan for either 1 day or a week using the Spoonacular API.
     * For single-day plans, calories are optionally targeted.
     * For multi-day plans, it averages daily nutrients across the requested days.
     *
     * @param targetKcal The desired daily calorie target (optional, used only for 1-day plans).
     * @param days Number of days to generate the plan for (1 or more).
     * @return A {@link MealPlanDTO} containing meals and nutrient summaries.
     * @throws ExternalApiQuotaException If the API quota is exceeded or an error occurs.
     */
    @Override
    public MealPlanDTO generateMealPlan(Integer targetKcal, int days) {
        log.debug("ADAPTER IN ⇢ kcal = {}", targetKcal);
        String frame = (days == 1) ? "day" : "week";

        try {
            if (frame.equals("day")) {
                MealPlanDTO dto = web.get()
                        .uri(uri -> uri.path(PLAN_ENDPOINT)
                                .queryParam("timeFrame", frame)
                                .queryParam("apiKey", apiKey)
                                .queryParamIfPresent("targetCalories",
                                        Optional.ofNullable(targetKcal))
                                .build())
                        .retrieve()
                        .bodyToMono(MealPlanDTO.class)
                        .block();

                if (dto == null || dto.meals() == null) {
                    throw new IllegalStateException("Spoonacular returned no meals");
                }

                return dto;
            }

            // For week plans
            WeeklyMealPlanDTO raw = web.get()
                    .uri(u -> u.path(PLAN_ENDPOINT)
                            .queryParam("timeFrame", "week")
                            .queryParam("apiKey", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(WeeklyMealPlanDTO.class)
                    .block();

            if (raw == null || raw.week() == null || raw.week().isEmpty()) {
                throw new IllegalStateException("Spoonacular returned no week-plan");
            }

            List<MealPlanDTO.Meal> meals = new ArrayList<>();
            int kcal = 0, protein = 0, carb = 0, fat = 0;
            Long index = 0L;
            int dayCounter = 0;

            for (WeeklyMealPlanDTO.Day d : raw.week().values()) {
                if (dayCounter == days) break;
                dayCounter++;

                kcal += d.nutrients().calories().intValue();
                carb += d.nutrients().carbohydrates().intValue();
                fat += d.nutrients().fat().intValue();
                protein += d.nutrients().protein().intValue();

                for (MealPlanDTO.Meal m : d.meals()) {
                    meals.add(new MealPlanDTO.Meal(
                            index++, m.id(), m.title(), m.imageType(),
                            m.readyInMinutes(), m.servings(), m.sourceUrl()));
                }
            }

            int perDayKcal = kcal / dayCounter;
            int perDayProtein = protein / dayCounter;
            int perDayCarb = carb / dayCounter;
            int perDayFat = fat / dayCounter;

            return new MealPlanDTO(
                    meals,
                    new MealPlanDTO.Nutrients(
                            BigDecimal.valueOf(perDayKcal),
                            BigDecimal.valueOf(perDayProtein),
                            BigDecimal.valueOf(perDayFat),
                            BigDecimal.valueOf(perDayCarb)
                    )
            );

        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new ExternalApiQuotaException("Spoonacular daily quota exhausted (429)");
            } else {
                throw new ExternalApiQuotaException("Spoonacular error " + e.getStatusCode() + " " + e.getMessage());
            }
        }
    }

    /**
     * Fetches detailed recipe information including nutrition from Spoonacular.
     * Uses caching to avoid repeat requests for the same recipe ID.
     *
     * @param id The ID of the recipe.
     * @return The full {@link RecipeDetailsDTO}.
     * @throws ExternalApiQuotaException if quota is exceeded or access is denied.
     */
    @Override
    @Cacheable(cacheNames = "recipes", key = "'recipe:' + #id")
    public RecipeDetailsDTO getRecipe(Long id) {
        log.info("⏩ Cache MISS → calling Spoonacular for recipe {}", id);
        try {
            return web.get()
                    .uri(INFO_ENDPOINT, id, apiKey)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, res ->
                            res.bodyToMono(String.class).flatMap(msg -> {
                                log.warn("❌ Spoonacular 4xx: {}", msg);
                                return Mono.error(new ExternalApiQuotaException("Spoonacular quota exceeded or access denied"));
                            }))
                    .onStatus(HttpStatusCode::is5xxServerError, res ->
                            Mono.error(new RuntimeException("Spoonacular server error")))
                    .bodyToMono(RecipeDetailsDTO.class)
                    .doOnNext(r -> log.debug("Recipe {} → title={}, url={}", id, r.title(), r.sourceUrl()))
                    .block();
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Optionally fetches raw nutrition widget data from Spoonacular for a recipe.
     *
     * @param id The recipe ID.
     * @return An optional {@link NutritionResponse} if available.
     */
    public Optional<NutritionResponse> fetchNutritionWidget(Long id) {
        String url = String.format("/recipes/%d/nutritionWidget.json?apiKey=%s", id, apiKey);

        try {
            return Optional.ofNullable(web.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(NutritionResponse.class)
                    .block());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
