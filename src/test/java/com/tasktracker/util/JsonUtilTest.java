package com.tasktracker.util;

import com.tasktracker.exceptions.JsonUtilException;
import com.tasktracker.model.Task;
import com.tasktracker.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonUtilTest {

    private static final LocalDateTime FIXED_TIME_1 = LocalDateTime.of(2026, 1, 15, 10, 30, 0);
    private static final LocalDateTime FIXED_TIME_2 = LocalDateTime.of(2026, 1, 15, 12, 45, 30);

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<JsonUtil> constructor = JsonUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()), "Constructor should be private");

        constructor.setAccessible(true);
        JsonUtil instance = constructor.newInstance();
        assertNotNull(instance, "Instance should not be null when instantiated via reflection");
    }

    @Test
    void writeNullListReturnsEmptyArray() {
        String result = JsonUtil.write(null);
        assertEquals("[]", result);
    }

    @Test
    void writeEmptyListReturnsEmptyArray() {
        String result = JsonUtil.write(Collections.emptyList());
        assertEquals("[]", result);
    }

    @Test
    void writeSingleTask() {
        Task task = new Task(1, "Buy groceries", TaskStatus.TO_DO, FIXED_TIME_1, FIXED_TIME_2);

        String json = JsonUtil.write(List.of(task));

        String expected = String.format(
                "[{\"id\":1,\"description\":\"Buy groceries\",\"status\":\"TO_DO\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}]",
                FIXED_TIME_1, FIXED_TIME_2
        );
        assertEquals(expected, json);
    }

    @Test
    void writeMultipleTasks() {
        Task task1 = new Task(1, "Task 1", TaskStatus.TO_DO, FIXED_TIME_1, FIXED_TIME_1);
        Task task2 = new Task(2, "Task 2", TaskStatus.IN_PROGRESS, FIXED_TIME_1, FIXED_TIME_2);
        Task task3 = new Task(3, "Task 3", TaskStatus.COMPLETED, FIXED_TIME_2, FIXED_TIME_2);

        String json = JsonUtil.write(List.of(task1, task2, task3));

        String expected = String.format(
                "[{\"id\":1,\"description\":\"Task 1\",\"status\":\"TO_DO\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}," +
                "{\"id\":2,\"description\":\"Task 2\",\"status\":\"IN_PROGRESS\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}," +
                "{\"id\":3,\"description\":\"Task 3\",\"status\":\"COMPLETED\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}]",
                FIXED_TIME_1, FIXED_TIME_1,
                FIXED_TIME_1, FIXED_TIME_2,
                FIXED_TIME_2, FIXED_TIME_2
        );
        assertEquals(expected, json);
    }

    @Test
    void writeTaskWithNullDescription() {
        Task task = new Task(1, null, TaskStatus.TO_DO, FIXED_TIME_1, FIXED_TIME_2);

        String json = JsonUtil.write(List.of(task));

        assertTrue(json.contains("\"description\":\"\""), "Null description should be serialized as empty string");
    }

    @Test
    void writeEscapesSpecialCharacters() {
        String specialDescription = "Line 1\nLine 2\rTab:\tBackslash:\\Quote:\"Hello\"";
        Task task = new Task(1, specialDescription, TaskStatus.TO_DO, FIXED_TIME_1, FIXED_TIME_2);

        String json = JsonUtil.write(List.of(task));

        assertTrue(json.contains("\\\\"), "Backslashes must be escaped as \\\\");
        assertTrue(json.contains("\\\"Hello\\\""), "Quotes must be escaped as \\\"");
        assertTrue(json.contains("\\n"), "Newlines must be escaped as \\n");
        assertTrue(json.contains("\\r"), "Carriage returns must be escaped as \\r");
        assertTrue(json.contains("\\t"), "Tabs must be escaped as \\t");
    }

    @Test
    void writeAllTaskStatuses() {
        for (TaskStatus status : TaskStatus.values()) {
            Task task = new Task(1, "Task " + status, status, FIXED_TIME_1, FIXED_TIME_2);
            String json = JsonUtil.write(List.of(task));
            assertTrue(json.contains("\"status\":\"" + status.name() + "\""));
        }
    }

    @Test
    void writeBoundaryIds() {
        Task taskZero = new Task(0, "Zero ID", TaskStatus.TO_DO, FIXED_TIME_1, FIXED_TIME_2);
        Task taskNegative = new Task(-1, "Negative ID", TaskStatus.TO_DO, FIXED_TIME_1, FIXED_TIME_2);
        Task taskMax = new Task(Integer.MAX_VALUE, "Max ID", TaskStatus.TO_DO, FIXED_TIME_1, FIXED_TIME_2);

        String json = JsonUtil.write(List.of(taskZero, taskNegative, taskMax));

        assertTrue(json.contains("\"id\":0"));
        assertTrue(json.contains("\"id\":-1"));
        assertTrue(json.contains("\"id\":" + Integer.MAX_VALUE));
    }

    @Test
    void readNullReturnsEmptyList() {
        List<Task> tasks = JsonUtil.read(null);
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }

    @Test
    void readEmptyStringReturnsEmptyList() {
        List<Task> tasks = JsonUtil.read("");
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }

    @Test
    void readBlankStringReturnsEmptyList() {
        List<Task> tasks = JsonUtil.read("   \t\n\r  ");
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }

    @Test
    void readEmptyArrayReturnsEmptyList() {
        List<Task> tasks = JsonUtil.read("[]");
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }

    @Test
    void readEmptyArrayWithWhitespaceReturnsEmptyList() {
        List<Task> tasks = JsonUtil.read("[   \n\t  ]");
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }

    @Test
    void readNotStartingWithBracketThrows() {
        JsonUtilException exception = assertThrows(JsonUtilException.class, () ->
                JsonUtil.read("{\"id\":1}]")
        );
        assertEquals("Malformed JSON array", exception.getMessage());
        assertNotNull(exception.getCause());
        assertInstanceOf(RuntimeException.class, exception.getCause());
    }

    @Test
    void readNotEndingWithBracketThrows() {
        JsonUtilException exception = assertThrows(JsonUtilException.class, () ->
                JsonUtil.read("[{\"id\":1}")
        );
        assertEquals("Malformed JSON array", exception.getMessage());
        assertNotNull(exception.getCause());
        assertInstanceOf(RuntimeException.class, exception.getCause());
    }

    @Test
    void readNonArrayThrows() {
        assertThrows(JsonUtilException.class, () -> JsonUtil.read("not a json"));
    }

    @Test
    void readMissingClosingBraceThrows() {
        String malformed = "[{\"id\":1,\"description\":\"test\"]";
        JsonUtilException exception = assertThrows(JsonUtilException.class, () ->
                JsonUtil.read(malformed)
        );
        assertEquals("Malformed JSON array: missing closing brace", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void readSingleTask() {
        String json = String.format(
                "[{\"id\":42,\"description\":\"Write unit tests\",\"status\":\"IN_PROGRESS\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}]",
                FIXED_TIME_1, FIXED_TIME_2
        );

        List<Task> tasks = JsonUtil.read(json);

        assertEquals(1, tasks.size());
        Task task = tasks.get(0);
        assertEquals(42, task.getId());
        assertEquals("Write unit tests", task.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(FIXED_TIME_1, task.getCreatedAt());
        assertEquals(FIXED_TIME_2, task.getUpdatedAt());
    }

    @Test
    void readMultipleTasks() {
        String json = String.format(
                "[{\"id\":1,\"description\":\"Task 1\",\"status\":\"TO_DO\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}," +
                "{\"id\":2,\"description\":\"Task 2\",\"status\":\"COMPLETED\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}]",
                FIXED_TIME_1, FIXED_TIME_1,
                FIXED_TIME_1, FIXED_TIME_2
        );

        List<Task> tasks = JsonUtil.read(json);

        assertEquals(2, tasks.size());
        assertEquals(1, tasks.get(0).getId());
        assertEquals("Task 1", tasks.get(0).getDescription());
        assertEquals(TaskStatus.TO_DO, tasks.get(0).getStatus());

        assertEquals(2, tasks.get(1).getId());
        assertEquals("Task 2", tasks.get(1).getDescription());
        assertEquals(TaskStatus.COMPLETED, tasks.get(1).getStatus());
    }

    @Test
    void readMultipleTasksWithWhitespace() {
        String json = String.format(
                "[\n" +
                "  {\n" +
                "    \"id\": 10,\n" +
                "    \"description\": \"Whitespace Task\",\n" +
                "    \"status\": \"TO_DO\",\n" +
                "    \"createdAt\": \"%s\",\n" +
                "    \"updatedAt\": \"%s\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 20,\n" +
                "    \"description\": \"Second Task\",\n" +
                "    \"status\": \"IN_PROGRESS\",\n" +
                "    \"createdAt\": \"%s\",\n" +
                "    \"updatedAt\": \"%s\"\n" +
                "  }\n" +
                "]",
                FIXED_TIME_1, FIXED_TIME_1,
                FIXED_TIME_2, FIXED_TIME_2
        );

        List<Task> tasks = JsonUtil.read(json);

        assertEquals(2, tasks.size());
        assertEquals(10, tasks.get(0).getId());
        assertEquals("Whitespace Task", tasks.get(0).getDescription());
        assertEquals(20, tasks.get(1).getId());
        assertEquals("Second Task", tasks.get(1).getDescription());
    }

    @Test
    void readUnescapesSafeEscapedSequences() {
        String json = String.format(
                "[{\"id\":1,\"description\":\"Line1\\nLine2\\tCol2\\rCR\\\\Backslash\",\"status\":\"TO_DO\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}]",
                FIXED_TIME_1, FIXED_TIME_2
        );

        List<Task> tasks = JsonUtil.read(json);

        assertEquals(1, tasks.size());
        assertEquals("Line1\nLine2\tCol2\rCR\\Backslash", tasks.get(0).getDescription());
    }

    @Test
    void readHandlesUnicodeAndPunctuation() {
        String json = String.format(
                "[{\"id\":1,\"description\":\"Café 🚀 - Punctuation: !@#$^&*()-_=+;:'|,.<>/?\",\"status\":\"TO_DO\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}]",
                FIXED_TIME_1, FIXED_TIME_2
        );

        List<Task> tasks = JsonUtil.read(json);

        assertEquals(1, tasks.size());
        assertEquals("Café 🚀 - Punctuation: !@#$^&*()-_=+;:'|,.<>/?", tasks.get(0).getDescription());
    }

    @Test
    void readAllTaskStatuses() {
        for (TaskStatus status : TaskStatus.values()) {
            String json = String.format(
                    "[{\"id\":1,\"description\":\"Test\",\"status\":\"%s\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}]",
                    status.name(), FIXED_TIME_1, FIXED_TIME_2
            );

            List<Task> tasks = JsonUtil.read(json);

            assertEquals(1, tasks.size());
            assertEquals(status, tasks.get(0).getStatus());
        }
    }

    @Test
    void readInvalidTaskStatusThrows() {
        String json = String.format(
                "[{\"id\":1,\"description\":\"Test\",\"status\":\"UNKNOWN_STATUS\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}]",
                FIXED_TIME_1, FIXED_TIME_2
        );

        assertThrows(IllegalArgumentException.class, () -> JsonUtil.read(json));
    }

    @Test
    void readInvalidCreatedAtThrows() {
        String json = String.format(
                "[{\"id\":1,\"description\":\"Test\",\"status\":\"TO_DO\",\"createdAt\":\"invalid-date\",\"updatedAt\":\"%s\"}]",
                FIXED_TIME_2
        );

        assertThrows(DateTimeParseException.class, () -> JsonUtil.read(json));
    }

    @Test
    void readInvalidUpdatedAtThrows() {
        String json = String.format(
                "[{\"id\":1,\"description\":\"Test\",\"status\":\"TO_DO\",\"createdAt\":\"%s\",\"updatedAt\":\"not-a-timestamp\"}]",
                FIXED_TIME_1
        );

        assertThrows(DateTimeParseException.class, () -> JsonUtil.read(json));
    }

    @Test
    void readNonNumericIdThrows() {
        String json = String.format(
                "[{\"id\":abc,\"description\":\"Test\",\"status\":\"TO_DO\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}]",
                FIXED_TIME_1, FIXED_TIME_2
        );

        assertThrows(NumberFormatException.class, () -> JsonUtil.read(json));
    }

    @Test
    void readMissingIdKeyDefaultsToMinusOne() {
        String json = String.format(
                "[{\"description\":\"Missing ID\",\"status\":\"TO_DO\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}]",
                FIXED_TIME_1, FIXED_TIME_2
        );

        List<Task> tasks = JsonUtil.read(json);

        assertEquals(1, tasks.size());
        assertEquals(-1, tasks.get(0).getId());
        assertEquals("Missing ID", tasks.get(0).getDescription());
    }

    @Test
    void readMissingDescriptionKeyDefaultsToEmptyString() {
        String json = String.format(
                "[{\"id\":5,\"status\":\"TO_DO\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}]",
                FIXED_TIME_1, FIXED_TIME_2
        );

        List<Task> tasks = JsonUtil.read(json);

        assertEquals(1, tasks.size());
        assertEquals(5, tasks.get(0).getId());
        assertEquals("", tasks.get(0).getDescription());
    }

    @Test
    void readMissingStatusKeyThrowsNullPointerException() {
        String json = String.format(
                "[{\"id\":1,\"description\":\"Clean room\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}]",
                FIXED_TIME_1, FIXED_TIME_2
        );

        assertThrows(NullPointerException.class, () -> JsonUtil.read(json));
    }

    @Test
    void readMissingCreatedAtKeyThrowsNullPointerException() {
        String json = String.format(
                "[{\"id\":1,\"description\":\"Clean room\",\"status\":\"TO_DO\",\"updatedAt\":\"%s\"}]",
                FIXED_TIME_2
        );

        assertThrows(NullPointerException.class, () -> JsonUtil.read(json));
    }

    @Test
    void readMissingUpdatedAtKeyThrowsNullPointerException() {
        String json = String.format(
                "[{\"id\":1,\"description\":\"Clean room\",\"status\":\"TO_DO\",\"createdAt\":\"%s\"}]",
                FIXED_TIME_1
        );

        assertThrows(NullPointerException.class, () -> JsonUtil.read(json));
    }

    @Test
    void roundTripEmptyList() {
        List<Task> original = List.of();
        String json = JsonUtil.write(original);
        List<Task> restored = JsonUtil.read(json);

        assertTrue(restored.isEmpty());
    }

    @Test
    void roundTripSingleTask() {
        Task original = new Task(101, "Deploy release", TaskStatus.IN_PROGRESS, FIXED_TIME_1, FIXED_TIME_2);

        String json = JsonUtil.write(List.of(original));
        List<Task> restored = JsonUtil.read(json);

        assertEquals(1, restored.size());
        assertTaskEquals(original, restored.get(0));
    }

    @Test
    void roundTripMultipleTasks() {
        List<Task> originalTasks = List.of(
                new Task(1, "First task", TaskStatus.TO_DO, FIXED_TIME_1, FIXED_TIME_1),
                new Task(2, "Second task", TaskStatus.IN_PROGRESS, FIXED_TIME_1, FIXED_TIME_2),
                new Task(3, "Third task", TaskStatus.COMPLETED, FIXED_TIME_2, FIXED_TIME_2)
        );

        String json = JsonUtil.write(originalTasks);
        List<Task> restoredTasks = JsonUtil.read(json);

        assertEquals(originalTasks.size(), restoredTasks.size());
        for (int i = 0; i < originalTasks.size(); i++) {
            assertTaskEquals(originalTasks.get(i), restoredTasks.get(i));
        }
    }

    @Test
    void roundTripWithSpecialCharacters() {
        String complexDesc = "Line 1\nLine 2\tTabbed\\With\\Backslashes\rCarriageReturn";
        Task original = new Task(7, complexDesc, TaskStatus.TO_DO, FIXED_TIME_1, FIXED_TIME_2);

        String json = JsonUtil.write(List.of(original));
        List<Task> restored = JsonUtil.read(json);

        assertEquals(1, restored.size());
        assertTaskEquals(original, restored.get(0));
    }

    @Test
    void roundTripWithEmptyDescription() {
        Task original = new Task(1, "", TaskStatus.TO_DO, FIXED_TIME_1, FIXED_TIME_2);

        String json = JsonUtil.write(List.of(original));
        List<Task> restored = JsonUtil.read(json);

        assertEquals(1, restored.size());
        assertTaskEquals(original, restored.get(0));
    }

    @Test
    void roundTripWithNullDescription() {
        Task original = new Task(1, null, TaskStatus.TO_DO, FIXED_TIME_1, FIXED_TIME_2);

        String json = JsonUtil.write(List.of(original));
        List<Task> restored = JsonUtil.read(json);

        assertEquals(1, restored.size());
        assertEquals(1, restored.get(0).getId());
        assertEquals("", restored.get(0).getDescription());
        assertEquals(TaskStatus.TO_DO, restored.get(0).getStatus());
        assertEquals(FIXED_TIME_1, restored.get(0).getCreatedAt());
        assertEquals(FIXED_TIME_2, restored.get(0).getUpdatedAt());
    }

    private void assertTaskEquals(Task expected, Task actual) {
        assertEquals(expected.getId(), actual.getId(), "Task ID mismatch");
        assertEquals(expected.getDescription(), actual.getDescription(), "Task description mismatch");
        assertEquals(expected.getStatus(), actual.getStatus(), "Task status mismatch");
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt(), "Task createdAt mismatch");
        assertEquals(expected.getUpdatedAt(), actual.getUpdatedAt(), "Task updatedAt mismatch");
    }
}
