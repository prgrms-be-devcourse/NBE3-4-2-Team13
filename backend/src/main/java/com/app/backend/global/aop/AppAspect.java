package com.app.backend.global.aop;

import com.app.backend.global.annotation.CustomPageJsonSerializer;
import com.app.backend.global.dto.response.ApiResponse;
import com.app.backend.global.module.CustomPageModule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.domain.Page;

@Slf4j
public class AppAspect {

    @Aspect
    public static class PageJsonSerializerAspect {

        private static final ConcurrentMap<String, ObjectMapper> objectMapperMap = new ConcurrentHashMap<>();

        @Around("@annotation(com.app.backend.global.annotation.CustomPageJsonSerializer)")
        public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method          method    = signature.getMethod();

            CustomPageJsonSerializer annotation = method.getAnnotation(CustomPageJsonSerializer.class);
            if (annotation == null)
                return joinPoint.proceed();

            Object result = joinPoint.proceed();

            if (result instanceof ApiResponse<?>) {
                ApiResponse<?> apiResponse = (ApiResponse<?>) result;
                Object         body        = apiResponse.getData();

                if (body instanceof Page<?>) {
                    String key = generateKey(annotation);
                    ObjectMapper objectMapper = objectMapperMap.computeIfAbsent(key, newKey -> {
                        log.info("새로운 ObjectMapper 생성: " + newKey);
                        ObjectMapper newObjectMapper = new ObjectMapper();
                        newObjectMapper.registerModules(new JavaTimeModule(), new CustomPageModule(annotation));
                        newObjectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                        return newObjectMapper;
                    });

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

        private String generateKey(CustomPageJsonSerializer annotation) {
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

}
