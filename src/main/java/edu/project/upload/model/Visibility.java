package edu.project.upload.model;

public enum Visibility {
    PRIVATE,
    PUBLIC;

    public static Visibility parseString(String value) {
        for (Visibility v : Visibility.values()) {
            if (v.name().equalsIgnoreCase(value)) {
                return v;
            }
        }
        return defaultValue();
    }

    public static Visibility defaultValue() {
        return PRIVATE;
    }
}
