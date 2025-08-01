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

    /**
     * Creates and configures a WebClient bean for communicating with the Spoonacular API.
     * Sets the base URL and default headers including JSON content type.
     * The API key is passed as a URI variable for convenience when building dynamic URLs.
     *
     * @param baseUrl The base URL of the Spoonacular API, injected from application properties.
     * @param apiKey The API key for authenticating with Spoonacular, also injected from properties.
     * @return A configured WebClient instance for making requests to Spoonacular.
     */
    @Bean
    public WebClient spoonacularWebClient(@Value("${spoonacular.base-url}") String baseUrl,
                                          @Value("${spoonacular.key}")      String apiKey) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Map.of("apiKey", apiKey))
                .build();
    }
    /**
     * Verifies connectivity to the Spoonacular API during application startup.
     * Makes a test request for one random recipe to validate the base URL and API key.
     * Logs a warning if the request fails but does not block the application from starting.
     *
     * @param spoonacularWebClient The injected WebClient used for the ping request.
     * @return A {@link CommandLineRunner} that runs once on startup.
     */

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
