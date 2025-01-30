package com.app.backend.global.util;

import java.lang.reflect.Field;

public class ReflectionUtil {

    public static void setPrivateFieldValue(Class<?> clazz,
                                            final Object obj,
                                            final String fieldName,
                                            final Object value) {
        try {
            Field field = null;

            while (clazz != null) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                    if (field != null) break;
                } catch (NoSuchFieldException e) {
                }
                clazz = clazz.getSuperclass();
            }

            if (field == null)
                throw new NoSuchFieldException("Unknown field name");

            field.setAccessible(true);
            field.set(obj, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
