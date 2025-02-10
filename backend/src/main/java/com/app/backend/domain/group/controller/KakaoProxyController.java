package com.app.backend.domain.group.controller;

import com.app.backend.domain.group.service.WebClientService;
import com.app.backend.global.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/proxy/kakao",
                produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class KakaoProxyController {

    private final WebClientService webClientService;

    @GetMapping("/geo")
    public Mono<ApiResponse<JsonNode>> getRegionCode(@RequestParam double x, @RequestParam double y) {
        return webClientService.fetchKakaoMapData(x, y)
                               .map(response -> {
                                   log.info("response: {}", response);
                                   return ApiResponse.of(true, HttpStatus.OK, "카카오맵 위치 정보 확인 완료", response);
                               });
    }

    @GetMapping("/address")
    public Mono<ApiResponse<JsonNode>> getCoordinatesByAddress(@RequestParam final String province,
                                                               @RequestParam final String city,
                                                               @RequestParam final String town) {
        return webClientService.fetchKakaoAddressByKeyword(province, city, town)
                               .map(response -> {
                                   log.info("response: {}", response);
                                   return ApiResponse.of(true, HttpStatus.OK, "카카오맵 주소 좌표 변환 완료", response);
                               });
    }

    @GetMapping("/region")
    public Mono<ApiResponse<JsonNode>> searchAddress(@RequestParam final String keyword) {
        return webClientService.fetchKakaoAddressByKeyword(keyword)
                               .map(response -> {
                                   log.info("response: {}", response);
                                   return ApiResponse.of(true, HttpStatus.OK, "카카오맵 주소 검색 완료", response);
                               });
    }

}
