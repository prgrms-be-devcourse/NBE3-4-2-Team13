package com.app.backend.global.annotation;

import com.app.backend.global.security.CustomWithSecurityContextFactory;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = CustomWithSecurityContextFactory.class)
public @interface CustomWithMockUser {

    long id() default 1L;

    String username() default "testUsername";

    String password() default "testPassword";

    String nickname() default "testNickname";

    String role() default "ROLE_USER";

}
