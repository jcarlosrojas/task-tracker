package com.tasktracker.service;

import com.tasktracker.model.Task;
import com.tasktracker.model.TaskStatus;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TaskService {
    private final List<Task> tasks;
    private int nextId;

    public TaskService() {
        this(List.of());
    }

    public TaskService(Collection<Task> initialTasks) {
        Objects.requireNonNull(initialTasks, "initialTasks is null");

        this.tasks = new ArrayList<>(initialTasks);
        this.nextId = this.tasks.stream()
                .mapToInt(Task::getId)
                .max()
                .orElse(0) + 1;
    }

    public Task add(String description) {
        String validDescription = validateDescription(description);

        Task task = new Task(nextId++, validDescription);
        tasks.add(task);
        return task;
    }

    public Task update(int id, String description) {
        validateId(id);

        Task task = findByIdOrThrow(id);
        task.setDescription(validateDescription(description));
        return task;
    }

    public Task markToDo(int id) {
        return updateStatus(id, TaskStatus.TO_DO);
    }

    public Task markInProgress(int id) {
        return updateStatus(id, TaskStatus.IN_PROGRESS);
    }

    public Task markCompleted(int id) {
        return updateStatus(id, TaskStatus.COMPLETED);
    }

    public Task updateStatus(int id, TaskStatus status) {
        validateId(id);
        Objects.requireNonNull(status, "status is null");

        Task task = findByIdOrThrow(id);
        task.setStatus(status);
        return task;
    }

    public boolean delete(int id) {
        validateId(id);
        return tasks.removeIf(task -> task.getId() == id);
    }

    public Optional<Task> findById(int id) {
        validateId(id);
        return tasks.stream()
                .filter(task -> task.getId() == id)
                .findFirst();
    }

    public List<Task> list() {
        return sortedTasks(tasks);
    }

    public List<Task> listByStatus(TaskStatus status) {
        Objects.requireNonNull(status, "status is null");

        return tasks.stream()
                .filter(task -> task.getStatus() == status)
                .sorted(Comparator.comparingInt(Task::getId))
                .toList();
    }

    private Task findByIdOrThrow(int id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
    }

    private String validateDescription(String description) {
        Objects.requireNonNull(description, "description is null");

        String trimmedDescription = description.trim();
        if (trimmedDescription.isEmpty()) {
            throw new IllegalArgumentException("Task description cannot be blank");
        }
        return trimmedDescription;
    }

    private void validateId(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("Task id must be greater than zero");
        }
    }

    private List<Task> sortedTasks(Collection<Task> tasks) {
        return tasks.stream()
                .sorted(Comparator.comparingInt(Task::getId))
                .toList();
    }
}
