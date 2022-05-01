package com.olezo.service.processor;

import javax.lang.model.element.TypeElement;

public class Util {
    private static final String GET_PREFIX = "get";

    public static String getClassName(TypeElement element) {
        return element.getSimpleName().toString();
    }

    public static String getClassNameWithPackage(TypeElement element) {
        return element.getQualifiedName().toString();
    }

    public static String makeFirstLetterUpperCase(String string) {
        char[] c = string.toCharArray();
        c[0] = Character.toUpperCase(c[0]);
        string = new String(c);

        return string;
    }

    public static String makeFirstLetterLowerCase(String string) {
        char[] c = string.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        string = new String(c);

        return string;
    }

    public static String buildFieldCallingMethod(String fieldName, String methodName) {
        return makeFirstLetterLowerCase(fieldName) + "." + methodName;
    }

    public static String buildGetterMethod(String fieldName) {
        return buildGetter(fieldName) + "()";
    }

    public static String buildGetter(String fieldName) {
        return GET_PREFIX + makeFirstLetterUpperCase(fieldName);
    }
}
