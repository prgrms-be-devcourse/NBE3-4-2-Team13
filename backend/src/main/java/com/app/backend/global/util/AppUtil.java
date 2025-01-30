package com.app.backend.global.util;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String localDateTimeToString(@NotNull final LocalDateTime localDateTime) {
        return DATE_TIME_FORMATTER.format(localDateTime);
    }

}
