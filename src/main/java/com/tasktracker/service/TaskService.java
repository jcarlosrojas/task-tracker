package com.tasktracker.service;

import com.tasktracker.model.Task;

public class TaskService {
    private int nextId = 0;

    public Task createTask(String description) {
        Task task = new Task(nextId, description);
        nextId++;
        return task;
    }
}
