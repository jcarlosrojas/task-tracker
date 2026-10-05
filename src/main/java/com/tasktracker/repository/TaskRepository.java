package com.tasktracker.repository;

import com.tasktracker.model.Task;
import com.tasktracker.util.JsonUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class TaskRepository {

    private final Path filePath;

    /**
     * Default constructor: uses the default "tasks.json" file in the working directory.
     */
    public TaskRepository() {
        this(Path.of("task.json"));
    }

    /**
     * Constructor that allows specifying a custom file path (useful for testing).
     */
    public TaskRepository(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads all tasks from the file.
     *
     * What to think about:
     * 1. What should happen if the file doesn't exist yet?
     * 2. What if the file is empty?
     * 3. How do you read the text from filePath?
     * 4. How do you pass the text to JsonUtil to get a List<Task>?
     *
     * @return a list of tasks, or an empty list if no tasks exist yet.
     */
    public List<Task> findAll() {
        try {
            if(Files.notExists(filePath)) {
                return List.of();
            }

            String json = Files.readString(filePath);

            if(json.isBlank()) {
                return List.of();
            }

            return Arrays.asList(JsonUtil.read(json, Task[].class));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves the given list of tasks into the file.
     *
     * What to think about:
     * 1. How do you turn the List<Task> into a JSON String using JsonUtil?
     * 2. How do you write that JSON String into filePath?
     *
     * @param tasks the list of tasks to save
     */
    public void saveAll(List<Task> tasks) {
        String json = JsonUtil.write(tasks);
        try {

            if(filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            Files.writeString(filePath, json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}