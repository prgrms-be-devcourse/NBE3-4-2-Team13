package com.app.backend.global.aop;

import com.app.backend.global.dto.response.ApiResponse;
import com.app.backend.global.module.CustomPageModule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.domain.Page;

@Slf4j
public class AppAspect {

    @Aspect
    public static class PageJsonSerializerAspect {

        private final ObjectMapper objectMapper;

        public PageJsonSerializerAspect() {
            objectMapper = new ObjectMapper();
            objectMapper.registerModules(new JavaTimeModule(), new CustomPageModule());
            objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }

        @Around("@annotation(com.app.backend.global.annotation.CustomPageJsonSerializer)")
        public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
            Object result = joinPoint.proceed();

            if (result instanceof ApiResponse<?>) {
                ApiResponse<?> apiResponse = (ApiResponse<?>) result;
                Object         body        = apiResponse.getData();

                if (body instanceof Page<?>) {
                    String   json     = objectMapper.writeValueAsString(body);
                    JsonNode jsonNode = objectMapper.readTree(json);
                    return ApiResponse.of(apiResponse.getIsSuccess(),
                                          apiResponse.getCode(),
                                          apiResponse.getMessage(),
                                          jsonNode);
                }
            }

            return result;
        }

    }

}
