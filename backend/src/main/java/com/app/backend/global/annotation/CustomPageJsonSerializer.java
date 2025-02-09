package com.app.backend.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomPageJsonSerializer {

    boolean content() default true;

    boolean hasContent() default true;

    boolean totalPages() default true;

    boolean totalElements() default true;

    boolean numberOfElements() default true;

    boolean size() default true;

    boolean number() default true;

    boolean hasPrevious() default true;

    boolean hasNext() default true;

    boolean isFirst() default true;

    boolean isLast() default true;

    boolean sort() default true;

    boolean empty() default true;

}
