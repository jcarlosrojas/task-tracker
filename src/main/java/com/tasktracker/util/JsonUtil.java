package com.tasktracker.util;

import com.tasktracker.exceptions.JsonUtilException;
import com.tasktracker.model.Task;
import com.tasktracker.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class JsonUtil {

    private JsonUtil() {
        // Utility class; prevent instantiation
    }

    /**
     * Converts a list of tasks into a formatted JSON string.
     */
    public static String write(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < tasks.size(); i++) {
            sb.append("{");
            sb.append("\"id\":").append(tasks.get(i).getId()).append(",");
            sb.append("\"description\":").append("\"").append(escape(tasks.get(i).getDescription())).append("\"").append(",");
            sb.append("\"status\":").append("\"").append(tasks.get(i).getStatus()).append("\"").append(",");
            sb.append("\"createdAt\":").append("\"").append(tasks.get(i).getCreatedAt()).append("\"").append(",");
            sb.append("\"updatedAt\":").append("\"").append(tasks.get(i).getUpdatedAt()).append("\"");
            sb.append("}");

            if (i != tasks.size() - 1) {
                sb.append(",");
            }
        }

        sb.append("]");

        return sb.toString();
    }

    /**
     * Helper to escape special characters in text so they don't break JSON syntax.
     */
    private static String escape(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Parses a JSON array string into a list of Task objects.
     */
    public static List<Task> read(String json) {

        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }

        json = json.trim();

        if (!json.startsWith("[") || !json.endsWith("]")) {
            throw new JsonUtilException("Malformed JSON array", new RuntimeException());
        }

        String content = json.substring(1, json.length() - 1);
        content = content.trim();

        if (content.isEmpty()) {
            return new ArrayList<>();
        }

        List<Task> tasks = new ArrayList<>();

        while (!content.isEmpty()) {

            int endIndex = content.indexOf("}");
            if (endIndex == -1) {
                throw  new JsonUtilException("Malformed JSON array: missing closing brace", null);
            }

            String objectBlock = content.substring(0, endIndex + 1);

            Task task = parseTask(objectBlock);
            tasks.add(task);

            content = content.substring(endIndex + 1);
            content = content.trim();

            if (content.startsWith(",")) {
                content = content.substring(1);
                content = content.trim();
            }
        }

        return tasks;
    }

    /**
     * Helper to parse a single JSON object block into a Task instance.
     */
    private static Task parseTask(String taskJson) {

        int id = extractIntValue(taskJson, "id");

        String rawDescription = extractStringValue(taskJson, "description");
        String description = unescape(rawDescription);

        String rawStatus = extractStringValue(taskJson, "status");
        TaskStatus status = TaskStatus.valueOf(rawStatus);

        String rawCreatedAt = extractStringValue(taskJson, "createdAt");
        LocalDateTime createdAt = LocalDateTime.parse(rawCreatedAt);

        String rawUpdatedAt = extractStringValue(taskJson, "updatedAt");
        LocalDateTime updatedAt =  LocalDateTime.parse(rawUpdatedAt);

        return new Task(id, description, status, createdAt, updatedAt);
    }

    /**
     * Helper to extract the text value enclosed in quotes for a given key.
     * Example: for key "status" in {"status":"TO_DO"}, returns "TO_DO".
     */
    private static String extractStringValue(String json, String key) {
        int index = json.indexOf(key);

        if (index == -1) {
            return null;
        }

        int start = index + key.length();
        int colonIndex = json.indexOf(":", start);
        int openQuoteIndex = json.indexOf("\"", colonIndex+1);
        int closeQuoteIndex = json.indexOf("\"", openQuoteIndex+1);

        return json.substring(openQuoteIndex+1, closeQuoteIndex).trim();
    }

    /**
     * Helper to extract an unquoted integer value for a given key.
     * Example: for key "id" in {"id":42,...}, returns 42.
     */
    private static int extractIntValue(String json, String key) {
        int index = json.indexOf(key);

        if (index == -1) {
            return -1;
        }

        int start = index + key.length();
        int colonIndex = json.indexOf(":", start);
        int commaIndex = json.indexOf(",", colonIndex);

        return Integer.parseInt(json.substring(colonIndex+1, commaIndex).trim());
    }

    /**
     * Helper to restore escaped characters back to original characters.
     */
    private static String unescape(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\\"", "\"").replace("\\n", "\n")
                .replace("\\r", "\r").replace("\\t", "\t")
                .replace("\\\\", "\\");
    }
}