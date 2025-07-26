package com.deyan.mealplanner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Configuration
@Profile("!test")
public class WebClientConfig {

    @Bean
    public WebClient spoonacularWebClient(@Value("${spoonacular.base-url}") String baseUrl,
                                          @Value("${spoonacular.key}")      String apiKey) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                // {apiKey} can be used in every URI you build later
                .defaultUriVariables(Map.of("apiKey", apiKey))
                .build();
    }

    @Bean
    CommandLineRunner pingSpoonacular(WebClient spoonacularWebClient) {
        return args -> {
            try {
                String json = spoonacularWebClient.get()
                        .uri("/recipes/random?number=1&apiKey={apiKey}")   // {apiKey} placeholder still resolved
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                System.out.println("✅ Spoonacular responded, JSON length = " + json.length());
            } catch (WebClientResponseException e) {
                // quota exhausted or bad key → 4xx/5xx
                System.out.println("⚠️  Spoonacular ping failed (" +
                        e.getStatusCode().value() + " " + e.getStatusText() +
                        ") – continuing startup");
            } catch (Exception e) {
                // DNS/offline/etc.
                System.out.println("⚠️  Spoonacular unreachable – continuing startup");
            }
        };
    }

}
