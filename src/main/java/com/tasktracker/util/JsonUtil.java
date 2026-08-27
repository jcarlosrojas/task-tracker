package com.tasktracker.util;

import com.tasktracker.exceptions.JsonUtilException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Objects;

public final class JsonUtil {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .build();

    private JsonUtil() {
        // Utility class; prevent instantiation
    }

    public static String write(Object value) {
        Objects.requireNonNull(value, "value cannot be null");

        try {
            return MAPPER.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new JsonUtilException(
                    "Could not serialize object to JSON",
                    exception
            );
        }
    }

    public static <T> T read(String json, Class<T> targetType) {
        validateJson(json);
        Objects.requireNonNull(targetType, "targetType cannot be null");

        try {
            return MAPPER.readValue(json, targetType);
        } catch (JacksonException exception) {
            throw new JsonUtilException(
                    "Could not deserialize JSON",
                    exception
            );
        }
    }

    public static <T> T read(
            String json,
            TypeReference<T> typeReference
    ) {
        validateJson(json);
        Objects.requireNonNull(typeReference, "typeReference cannot be null");

        try {
            return MAPPER.readValue(json, typeReference);
        } catch (JacksonException exception) {
            throw new JsonUtilException(
                    "Could not deserialize JSON",
                    exception
            );
        }
    }

    public static JsonNode readTree(String json) {
        validateJson(json);

        try {
            return MAPPER.readTree(json);
        } catch (JacksonException exception) {
            throw new JsonUtilException(
                    "Could not parse JSON",
                    exception
            );
        }
    }

    public static boolean isValid(String json) {
        if (json == null || json.isBlank()) {
            return false;
        }

        try {
            MAPPER.readTree(json);
            return true;
        } catch (JacksonException exception) {
            return false;
        }
    }

    private static void validateJson(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException(
                    "JSON cannot be null or blank"
            );
        }
    }
}