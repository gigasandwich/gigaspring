package com.giga.spring.util.reflect;

import java.lang.reflect.*;
import java.time.*;
import java.util.*;

public class Parser {
    private static List<Class<?>> supportedClasses;

    private static Parser instance;

    public static Parser getInstance() {
        if (instance == null) {
            supportedClasses = List
                    .of(String.class,
                        int.class, long.class, double.class, float.class,
                        Integer.class, Long.class, Double.class, Float.class,
                        LocalDate.class, LocalTime.class, LocalDateTime.class, YearMonth.class);
            return new Parser();
        }
        return instance;
    }

    public boolean canSupport(Class<?> c) {
        return supportedClasses.contains(c);
    }

    public <T> T stringToTargetType(String s, Class<T> c) {
        if (s == null) return null;

        Object o = null;

        // String
        if (c.equals(String.class)) o = s;

        // Numeric
        else if (c.equals(int.class) || c.equals(Integer.class)) o =  Integer.valueOf(s);
        else if (c.equals(long.class) || c.equals(Long.class)) o = Long.valueOf(s);
        else if (c.equals(double.class) || c.equals(Double.class)) o =  Double.valueOf(s);
        else if (c.equals(float.class) || c.equals(Float.class)) o = Float.valueOf(s);

        // Time
        else if (c.equals(LocalDate.class)) o = LocalDate.parse(s);
        else if (c.equals(LocalTime.class)) o =  LocalTime.parse(s);
        else if (c.equals(LocalDateTime.class)) o = LocalDateTime.parse(s);
        else if (c.equals(YearMonth.class)) o = YearMonth.parse(s);

        if (o == null) return null;

        return (T) o;
    }
}
