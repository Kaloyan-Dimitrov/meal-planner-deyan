package com.deyan.mealplanner.service.impl;

import com.deyan.mealplanner.dto.MealPlanDTO;
import com.deyan.mealplanner.dto.NutritionResponse;
import com.deyan.mealplanner.dto.RecipeDetailsDTO;
import com.deyan.mealplanner.dto.WeeklyMealPlanDTO;
import com.deyan.mealplanner.service.interfaces.RecipeAPIAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!test")
public class SpoonacularAdapter implements RecipeAPIAdapter {
    private final WebClient web;

    @Value("${spoonacular.key}")      // injected from .env → application.properties
    private String apiKey;

    /* ---------------------------------------------------------------------------
       ENDPOINT TEMPLATES
       -------------------------------------------------------------------------*/
    private static final String PLAN_ENDPOINT  = "/mealplanner/generate";
    private static final String INFO_ENDPOINT  =
            "/recipes/{id}/information?includeNutrition=true&apiKey={key}";

    /* ---------------------------------------------------------------------------
       ==========  D A Y / W E E K   P L A N   G E N E R A T I O N  ==========
       -------------------------------------------------------------------------*/
    @Override
    public MealPlanDTO generateMealPlan(Integer targetKcal, int days) {
        log.debug("ADAPTER IN   ⇢  kcal = {}", targetKcal);
        String frame = (days == 1) ? "day" : "week";

        try {
            if(frame.equals("day")) {
                MealPlanDTO dto = web.get()
                        .uri(uri -> uri.path(PLAN_ENDPOINT)
                                .queryParam("timeFrame", frame)
                                .queryParam("apiKey",   apiKey)
                                .queryParamIfPresent("targetCalories",
                                        frame.equals("day")
                                                ? Optional.ofNullable(targetKcal)
                                                : Optional.empty())
                                .build())
                        .retrieve()
                        .bodyToMono(MealPlanDTO.class)
                        .block();

                if (dto == null || dto.meals() == null) {
                    throw new IllegalStateException("Spoonacular returned no meals " +
                            "for frame=" + frame + ", kcal=" + targetKcal);
                }

                log.info("Spoonacular {} {}kcal → {} meals",
                        frame, targetKcal == null ? "?" : targetKcal,
                        dto.meals().size());

                return dto;
            }

            WeeklyMealPlanDTO raw = web.get()
                    .uri(u -> u.path(PLAN_ENDPOINT)
                            .queryParam("timeFrame", "week")
                            .queryParam("apiKey",    apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(WeeklyMealPlanDTO.class)
                    .block();

            if(raw==null || raw.week()==null ||  raw.week().isEmpty()){
                throw new IllegalStateException("Spoonacular returned no week-plan");
            }
            List<MealPlanDTO.Meal> meals = new ArrayList<>();
            int kcal = 0;
            int protein = 0;
            int carb = 0;
            int fat = 0;
            Long index = 0L;
            int dayCounter=0;
            for (WeeklyMealPlanDTO.Day d : raw.week().values()) {
                if (dayCounter == days) break;   // ← stop when we've hit the requested days
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
            int perDayKcal    = kcal    / dayCounter;
            int perDayProtein = protein / dayCounter;
            int perDayCarb    = carb    / dayCounter;
            int perDayFat     = fat     / dayCounter;


            return new MealPlanDTO(
                        meals,
                        new MealPlanDTO.Nutrients(BigDecimal.valueOf(perDayKcal), (BigDecimal.valueOf(perDayProtein))
                                , (BigDecimal.valueOf(perDayFat)), (BigDecimal.valueOf(perDayCarb))));

        } catch (WebClientResponseException e) {
            // helpful diagnostics in the log
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.error("Spoonacular daily quota exhausted (429).");
            } else {
                log.error("Spoonacular error {} – {}", e.getStatusCode(), e.getResponseBodyAsString());
            }
            throw e;    // propagate so the service layer can decide what to do
        }
    }

    /* ---------------------------------------------------------------------------
       ==========  R E C I P E   D E T A I L S  ==========
       -------------------------------------------------------------------------*/
    @Override
    public RecipeDetailsDTO getRecipe(Long id) {

        RecipeDetailsDTO dto =  web.get()
                .uri(INFO_ENDPOINT, id, apiKey)
                .retrieve()
                .bodyToMono(RecipeDetailsDTO.class)
                .doOnNext(r -> log.debug("Recipe {} → title={}, url={}", id, r.title(), r.sourceUrl()))
                .block();
        log.info("Nutrition for recipe {} → {}", id, dto.nutrition());
    return dto;
    }
    public Optional<NutritionResponse> fetchNutritionWidget(Long id) {
        String url = String.format("/recipes/%d/nutritionWidget.json?apiKey=%s", id, apiKey);

        try {
            return Optional.ofNullable(web.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(NutritionResponse.class)
                    .block());
        } catch (Exception e) {
            log.warn("Failed to fetch nutritionWidget for id={}", id, e);
            return Optional.empty();
        }
    }

}
