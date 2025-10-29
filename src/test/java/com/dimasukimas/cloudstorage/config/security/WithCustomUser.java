package com.dimasukimas.cloudstorage.config.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@WithSecurityContext(factory = WithCustomSecurityContextFactory.class)
public @interface WithCustomUser {

    long id() default 1L;

    String username() default "testUser";

    String password() default "password";

    String role() default "ROLE_USER";
}
