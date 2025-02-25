package com.app.backend.global.util;

import jakarta.validation.constraints.NotNull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class AppUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat        DATE_FORMAT         = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String localDateTimeToString(@NotNull final LocalDateTime localDateTime) {
        return DATE_TIME_FORMATTER.format(localDateTime);
    }

    public static String DateToString(@NotNull final Date date) {
        return DATE_FORMAT.format(date);
    }

}
