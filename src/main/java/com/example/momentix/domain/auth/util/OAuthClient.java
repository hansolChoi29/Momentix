package com.example.momentix.domain.auth.util;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class OAuthClient {
    private final WebClient.Builder webClientBuilder;

    public String get(String url, String accessToken) {
        return webClientBuilder.build()
                .get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String postForm(String url, String... formData) {
        StringBuilder bodyBuilder = new StringBuilder();
        for (int i = 0; i < formData.length; i += 2) {
            if (i > 0) bodyBuilder.append("&");
            bodyBuilder.append(formData[i]).append("=").append(formData[i + 1]);
        }
        return webClientBuilder.build()
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(bodyBuilder.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
