package com.giga.spring.util.reflect;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelParser {
    private static ModelParser instance;

    public static ModelParser getInstance() {
        if (instance == null)
            return new ModelParser();
        return instance;
    }

    public <T> void bind(Object model, String[] fieldTree, int f, String strValue) throws Exception {
        Pattern pattern = Pattern.compile("([A-Za-z_$][A-Za-z0-9_$]*)(?:\\[(\\d+)\\])*");
        Matcher matcher = pattern.matcher(fieldTree[f]);

        if (!matcher.matches())
            throw new Exception("Invalid field " + fieldTree[f]);

        String fieldName = matcher.group(1);

        // Collect all indexes like [0][1] -> [0,1]
        Pattern idxPattern = Pattern.compile("\\[(\\d+)\\]");
        Matcher idxMatcher = idxPattern.matcher(fieldTree[f]);
        List<Integer> indexes = new ArrayList<>();
        while (idxMatcher.find()) {
            indexes.add(Integer.parseInt(idxMatcher.group(1)));
        }

        Field field = model.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);

        Type fieldGenericType = field.getGenericType();

        Parser parser = Parser.getInstance();
        if (!(fieldGenericType instanceof Class<?> clazz))
            return;

        // Simple object (no indexes)
        if (!clazz.isArray() && indexes.isEmpty()) {
            if (parser.canSupport(clazz))
                field.set(model, Parser.getInstance().stringToTargetType(strValue, clazz));
            else {
                Object subModel = ReflectionUtil.getInstance().newInstanceFromNoArgsConstructor(clazz);
                bind(subModel, fieldTree, f + 1, strValue);
                field.set(model, subModel);
            }
            return;
        }

        // Arrays, can be multidimentional too
        if (clazz.isArray()) {
            // Arrays stack: arrays.get(0) is the outermost array (might be null)
            List<Object> arrays = new ArrayList<>();
            Object outer = field.get(model);
            arrays.add(outer);

            Class<?> currentArrayClass = clazz;

            // Traverse indexes to prepare arrays list
            for (int level = 0; level < indexes.size(); level++) {
                int idx = indexes.get(level);
                Class<?> elementClassAtThisLevel = currentArrayClass.getComponentType();

                Object parentArray = arrays.get(level);

                if (level == indexes.size() - 1) {
                    // Last level: set element into parentArray
                    Class<?> ultimate = elementClassAtThisLevel;
                    while (ultimate.isArray())
                        ultimate = ultimate.getComponentType();

                    if (parser.canSupport(ultimate)) {
                        Object element = Parser.getInstance().stringToTargetType(strValue, ultimate);

                        Object updatedParent = addToArrayOrReplace(parentArray, idx, element, elementClassAtThisLevel);
                        arrays.set(level, updatedParent);
                    } else {
                        Object element = ReflectionUtil.getInstance().newInstanceFromNoArgsConstructor(ultimate);
                        Object updatedParent = addToArrayOrReplace(parentArray, idx, element, elementClassAtThisLevel);
                        arrays.set(level, updatedParent);
                        bind(element, fieldTree, f + 1, strValue);
                    }

                    // Propagate updated arrays up to the top and assign to field
                    for (int j = level; j > 0; j--) {
                        Object child = arrays.get(j);
                        Object parent = arrays.get(j - 1);
                        int parentIdx = indexes.get(j - 1);
                        Class<?> parentElementClass = (parent == null && j - 1 == 0) ? currentArrayClass.getComponentType() : parent.getClass().getComponentType();
                        Object updatedParent = addToArrayOrReplace(parent, parentIdx, child, parentElementClass);
                        arrays.set(j - 1, updatedParent);
                    }

                    // Set top-level field
                    field.set(model, arrays.get(0));
                    return;
                } else {
                    // Intermediate level: ensure inner array exists and add to arrays list
                    Object inner = null;
                    if (parentArray != null) {
                        int len = Array.getLength(parentArray);
                        if (idx < len)
                            inner = Array.get(parentArray, idx);
                    }

                    if (inner == null) {
                        Object newInner = Array.newInstance(elementClassAtThisLevel.getComponentType(), 0);
                        Object updatedParent = addToArrayOrReplace(parentArray, idx, newInner, elementClassAtThisLevel);
                        arrays.set(level, updatedParent);
                        inner = Array.get(updatedParent, idx);
                    } else {
                        // Keep existing parentArray in list
                        arrays.set(level, parentArray);
                    }

                    // Append inner for next level
                    if (arrays.size() > level + 1)
                        arrays.set(level + 1, inner);
                    else
                        arrays.add(inner);

                    currentArrayClass = elementClassAtThisLevel;
                }
            }
        }
    }

    private Object addToArrayOrReplace(Object array, int i, Object element, Class<?> elementClass) {
        if (array == null) {
            Object newArray  = Array.newInstance(elementClass, i + 1);
            Array.set(newArray, i, element);
            return newArray;
        }

        int length = Array.getLength(array);
        int newLength = Math.max(i + 1, length);

        if (newLength > length) {
            Object newArray = Array.newInstance(elementClass, newLength);

            for (int j = 0; j < length; j++)
                Array.set(newArray, j, Array.get(array, j));

            Array.set(newArray, i, element);
            return newArray;
        } else {
            Array.set(array, i, element);
            return array;
        }
    }


    public List<String> getObjectToStringPatterns(HttpServletRequest req, Parameter parameter) {
        return req.getParameterMap()
                .keySet()
                .stream()
                .filter(s -> s.startsWith(parameter.getName() + "."))
                .toList();
    }
}
