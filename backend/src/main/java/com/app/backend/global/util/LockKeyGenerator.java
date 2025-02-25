package com.app.backend.global.util;

import jakarta.validation.constraints.NotNull;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class LockKeyGenerator {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    public static String generateLockKey(final ProceedingJoinPoint joinPoint, @NotNull final String spelExpression) {
        MethodSignature signature      = (MethodSignature) joinPoint.getSignature();
        Object[]        args           = joinPoint.getArgs();
        Method          method         = signature.getMethod();
        String[]        parameterNames = signature.getParameterNames();

        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++)
            context.setVariable(parameterNames[i], args[i]);

        Object value = PARSER.parseExpression(spelExpression).getValue(context);

        if (value == null)
            throw new IllegalArgumentException("Lock key cannot be null");

        return "%s:%s".formatted(method.getName(), convertToKey(value));
    }

    private static String convertToKey(final Object value) {
        if (value instanceof String)
            return (String) value;
        else if (value instanceof Number || value instanceof Boolean)
            return String.valueOf(value);
        else if (value instanceof Enum)
            return ((Enum<?>) value).name();
        else if (value instanceof LocalDateTime)
            return AppUtil.localDateTimeToString((LocalDateTime) value);
        else if (value instanceof Date)
            return AppUtil.DateToString((Date) value);
        else if (value.getClass().isArray())
            return arrayToString(value);
        else if (value instanceof Collection<?>)
            return collectionToString((Collection<?>) value);
        else if (value instanceof Map<?, ?>)
            return mapToString((Map<?, ?>) value);
        else
            return value.toString();
    }

    private static String arrayToString(final Object array) {
        List<String> elements = new ArrayList<>();
        for (int i = 0; i < Array.getLength(array); i++)
            elements.add(convertToKey(Array.get(array, i)));
        return "[%s]".formatted(String.join(",", elements));
    }

    private static String collectionToString(final Collection<?> collection) {
        List<String> elements = new ArrayList<>();
        for (Object item : collection)
            elements.add(convertToKey(item));
        return "[%s]".formatted(String.join(",", elements));
    }

    private static String mapToString(final Map<?, ?> map) {
        List<String> entries = new ArrayList<>();
        for (Map.Entry<?, ?> entry : map.entrySet())
            entries.add(convertToKey(entry.getKey()) + "=" + convertToKey(entry.getValue()));
        return "{%s}".formatted(String.join(",", entries));
    }

}
