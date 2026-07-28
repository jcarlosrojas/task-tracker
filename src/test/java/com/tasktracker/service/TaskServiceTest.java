package com.tasktracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tasktracker.model.Task;
import com.tasktracker.model.TaskStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TaskServiceTest {

    @Test
    void startsWithNoTasks() {
        TaskService service = new TaskService();

        assertTrue(service.list().isEmpty());
    }

    @Test
    void rejectsNullInitialTasks() {
        assertThrows(NullPointerException.class, () -> new TaskService(null));
    }

    @Test
    void startsNewTaskIdsAfterTheHighestInitialId() {
        TaskService service = new TaskService(List.of(task(4, "Existing"), task(9, "Latest")));

        Task addedTask = service.add("New task");

        assertEquals(10, addedTask.getId());
    }

    @Test
    void addTrimsDescriptionAndUsesToDoStatus() {
        TaskService service = new TaskService();

        Task addedTask = service.add("  Write tests  ");

        assertEquals(1, addedTask.getId());
        assertEquals("Write tests", addedTask.getDescription());
        assertEquals(TaskStatus.TO_DO, addedTask.getStatus());
        assertEquals(List.of(addedTask), service.list());
    }

    @Test
    void addRejectsNullDescription() {
        TaskService service = new TaskService();

        assertThrows(NullPointerException.class, () -> service.add(null));
        assertTrue(service.list().isEmpty());
    }

    @Test
    void addRejectsBlankDescription() {
        TaskService service = new TaskService();

        assertThrows(IllegalArgumentException.class, () -> service.add("  \t"));
        assertTrue(service.list().isEmpty());
    }

    @Test
    void updateChangesAndTrimsDescription() {
        TaskService service = new TaskService();
        Task task = service.add("Original");

        Task updatedTask = service.update(task.getId(), "  Updated  ");

        assertSame(task, updatedTask);
        assertEquals("Updated", updatedTask.getDescription());
    }

    @Test
    void updateRejectsInvalidDescriptionWithoutChangingTask() {
        TaskService service = new TaskService();
        Task task = service.add("Original");

        assertThrows(IllegalArgumentException.class, () -> service.update(task.getId(), " "));

        assertEquals("Original", task.getDescription());
    }

    @Test
    void updateRejectsUnknownOrInvalidIds() {
        TaskService service = new TaskService();

        assertThrows(IllegalArgumentException.class, () -> service.update(1, "Updated"));
        assertThrows(IllegalArgumentException.class, () -> service.update(0, "Updated"));
    }

    @Test
    void markMethodsSetTheExpectedStatus() {
        TaskService service = new TaskService();
        Task task = service.add("Task");

        service.markInProgress(task.getId());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());

        service.markCompleted(task.getId());
        assertEquals(TaskStatus.COMPLETED, task.getStatus());

        service.markToDo(task.getId());
        assertEquals(TaskStatus.TO_DO, task.getStatus());
    }

    @Test
    void updateStatusRejectsNullStatus() {
        TaskService service = new TaskService();
        Task task = service.add("Task");

        assertThrows(NullPointerException.class, () -> service.updateStatus(task.getId(), null));
        assertEquals(TaskStatus.TO_DO, task.getStatus());
    }

    @Test
    void deleteReturnsWhetherAValidTaskWasRemoved() {
        TaskService service = new TaskService();
        Task task = service.add("Task");

        assertTrue(service.delete(task.getId()));
        assertFalse(service.delete(task.getId()));
        assertTrue(service.list().isEmpty());
    }

    @Test
    void deleteRejectsInvalidIds() {
        TaskService service = new TaskService();

        assertThrows(IllegalArgumentException.class, () -> service.delete(0));
    }

    @Test
    void findByIdReturnsMatchingTaskOrEmpty() {
        TaskService service = new TaskService();
        Task task = service.add("Task");

        assertEquals(Optional.of(task), service.findById(task.getId()));
        assertEquals(Optional.empty(), service.findById(2));
    }

    @Test
    void findByIdRejectsInvalidIds() {
        TaskService service = new TaskService();

        assertThrows(IllegalArgumentException.class, () -> service.findById(0));
    }

    @Test
    void listReturnsTasksSortedByIdWithoutExposingInternalList() {
        Task highIdTask = task(7, "High");
        Task lowIdTask = task(2, "Low");
        TaskService service = new TaskService(List.of(highIdTask, lowIdTask));

        List<Task> listedTasks = service.list();

        assertEquals(List.of(lowIdTask, highIdTask), listedTasks);
        assertThrows(UnsupportedOperationException.class, () -> listedTasks.add(task(8, "New")));
    }

    @Test
    void listByStatusFiltersAndSortsTasksById() {
        Task completedTask = task(5, "Completed", TaskStatus.COMPLETED);
        Task todoTask = task(2, "To do", TaskStatus.TO_DO);
        Task earlierCompletedTask = task(1, "Earlier completed", TaskStatus.COMPLETED);
        TaskService service = new TaskService(List.of(completedTask, todoTask, earlierCompletedTask));

        assertEquals(
                List.of(earlierCompletedTask, completedTask),
                service.listByStatus(TaskStatus.COMPLETED));
        assertEquals(List.of(todoTask), service.listByStatus(TaskStatus.TO_DO));
    }

    @Test
    void listByStatusRejectsNullStatus() {
        TaskService service = new TaskService();

        assertThrows(NullPointerException.class, () -> service.listByStatus(null));
    }

    private static Task task(int id, String description) {
        return task(id, description, TaskStatus.TO_DO);
    }

    private static Task task(int id, String description, TaskStatus status) {
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 1, 12, 0);
        return new Task(id, description, status, timestamp, timestamp);
    }

}
