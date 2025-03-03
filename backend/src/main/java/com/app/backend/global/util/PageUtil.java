package com.app.backend.global.util;

import com.app.backend.global.annotation.CustomPageJsonSerializer;
import com.app.backend.global.dto.response.ApiResponse;
import com.app.backend.global.module.CustomPageModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

@Slf4j
public class PageUtil {

    private static final ConcurrentMap<String, ObjectMapper> objectMapperMap = new ConcurrentHashMap<>();

    public static ResponseEntity<?> processResponseEntity(final ResponseEntity<?> responseEntity,
                                                          final CustomPageJsonSerializer annotation) {
        Object body = responseEntity.getBody();
        if (body instanceof Page<?> page)
            return ResponseEntity.ok(processPageJson(page, annotation));
        else if (body instanceof ApiResponse<?> apiResponse)
            return ResponseEntity.ok(processApiResponse(apiResponse, annotation));
        return responseEntity;
    }

    public static ApiResponse<?> processApiResponse(final ApiResponse<?> apiResponse,
                                                    final CustomPageJsonSerializer annotation) {
        if (apiResponse.getData() instanceof Page<?> page)
            return ApiResponse.of(apiResponse.getIsSuccess(),
                                  apiResponse.getCode(),
                                  apiResponse.getMessage(),
                                  processPageJson(page, annotation));
        return apiResponse;
    }

    public static JsonNode processPageJson(final Page<?> page, final CustomPageJsonSerializer annotation) {
        String key = generateKey(annotation);
        ObjectMapper objectMapper = objectMapperMap.computeIfAbsent(key, newKey -> {
            log.info("새로운 ObjectMapper 생성: {}", newKey);
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModules(new JavaTimeModule(), new CustomPageModule(annotation));
            mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper;
        });

        try {
            String json = objectMapper.writeValueAsString(page);
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("Json processing exception occurred", e);
            return null;
        }
    }

    private static String generateKey(CustomPageJsonSerializer annotation) {
        return annotation.content() + "_" +
               annotation.hasContent() + "_" +
               annotation.totalPages() + "_" +
               annotation.totalElements() + "_" +
               annotation.numberOfElements() + "_" +
               annotation.size() + "_" +
               annotation.number() + "_" +
               annotation.hasPrevious() + "_" +
               annotation.hasNext() + "_" +
               annotation.isFirst() + "_" +
               annotation.isLast() + "_" +
               annotation.sort() + "_" +
               annotation.empty();
    }

}
