package com.app.backend.domain.group.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WebClientService {

    private static final String KAKAO_MAP_API_URL = "https://dapi.kakao.com/v2/local";

    private final WebClient.Builder webClientBuilder;

    @Value("${kakao.rest-api.key}")
    private String kakaoJsKey;

    public Mono<JsonNode> fetchKakaoMapData(final double x, final double y) {
        WebClient webClient = getKakaoMapWebClient();
        return webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/geo/coord2address.json")
                                                     .queryParam("x", x)
                                                     .queryParam("y", y)
                                                     .queryParam("input_coord", "WGS84")
                                                     .build())
                        .retrieve()
                        .bodyToMono(JsonNode.class);
    }

    public Mono<JsonNode> fetchKakaoAddressByKeyword(final String province, final String city, final String town) {
        WebClient webClient = getKakaoMapWebClient();
        return webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/search/address.json")
                                                     .queryParam("query", "%s %s %s".formatted(province, city, town))
                                                     .build())
                        .retrieve()
                        .bodyToMono(JsonNode.class);
    }

    public Mono<JsonNode> fetchKakaoAddressByKeyword(final String keyword) {
        WebClient webClient = getKakaoMapWebClient();
        return webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/search/keyword.json")
                                                     .queryParam("query", keyword)
                                                     .build())
                        .retrieve()
                        .bodyToMono(JsonNode.class);
    }

    //============================== 내부 메서드 ==============================//

    private WebClient getKakaoMapWebClient() {
        String apiKey = "KakaoAK %s".formatted(kakaoJsKey);
        WebClient webClient = webClientBuilder.baseUrl(KAKAO_MAP_API_URL)
                                              .defaultHeader(HttpHeaders.AUTHORIZATION, apiKey).build();
        return webClient;
    }

}
