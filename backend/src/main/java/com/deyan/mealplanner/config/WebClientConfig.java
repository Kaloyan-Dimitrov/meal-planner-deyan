package com.deyan.mealplanner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Configuration
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
            String json = spoonacularWebClient.get()
                    .uri("/recipes/random?number=1&apiKey={apiKey}") // the {apiKey} placeholder is resolved
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("✅ Spoonacular responded, JSON length = " + json.length());
        };
    }

}
