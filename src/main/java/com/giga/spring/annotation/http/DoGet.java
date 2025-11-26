package com.giga.spring.annotation.http;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DoGet {
    String path() default "";
}
