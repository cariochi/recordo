package com.cariochi.recordo.core.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EnvironmentHolder {

    private static Object environment; // typed as Object to avoid hard Spring dependency

    public static void set(Object environment) {
        EnvironmentHolder.environment = environment;
    }

    public static String get(String key) {
        if (environment == null) {
            return null;
        }
        try {
            // Reflectively call Environment.getProperty("recordo.<key>")
            return (String) environment.getClass()
                    .getMethod("getProperty", String.class)
                    .invoke(environment, "recordo." + key);
        } catch (Exception ignored) {
            return null;
        }
    }
}
