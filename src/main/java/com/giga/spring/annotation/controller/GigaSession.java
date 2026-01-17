package com.giga.spring.annotation.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Added from the refactor PR
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface GigaSession {
    
}
