package com.tasktracker.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tasktracker.model.Task;
import com.tasktracker.model.TaskStatus;
import com.tasktracker.service.TaskService;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandParserTest {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private PrintStream originalOut;
    private CommandParser parser;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream, true, StandardCharsets.UTF_8));
        parser = new CommandParser();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void parsePrintsHelpWhenNoArgumentsAreProvided() {
        parser.parse(new String[] {});

        assertOutput(
                """
                Available commands:
                  add "task description"
                  update <id> "new description"
                  delete <id>
                  list
                """);
    }

    @Test
    void parsePrintsUnknownCommandAndHelpForUnsupportedCommand() {
        parser.parse(new String[] {"complete", "1"});

        assertOutput(
                """
                Unknown command: complete
                Available commands:
                  add "task description"
                  update <id> "new description"
                  delete <id>
                  list
                """);
    }

    @Test
    void parseTreatsCommandNamesCaseInsensitively() {
        parser.parse(new String[] {"ADD", "Write tests"});

        assertOutput("Adding task: Write tests%n");
    }

    @Test
    void parseThrowsWhenArgumentsArrayIsNull() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    void addPrintsUsageWhenDescriptionIsMissing() {
        parser.parse(new String[] {"add"});

        assertOutput("Usage: add \"task description\"%n");
    }

    @Test
    void addPrintsDescriptionWhenProvided() {
        parser.parse(new String[] {"add", "Buy milk"});

        assertOutput("Adding task: Buy milk%n");
    }

    @Test
    void addPrintsUsageWhenExtraArgumentsAreProvided() {
        parser.parse(new String[] {"add", "Buy milk", "ignored"});

        assertOutput("Usage: add \"task description\"%n");
    }

    @Test
    void addPrintsUsageWhenDescriptionIsBlank() {
        parser.parse(new String[] {"add", ""});

        assertOutput("Usage: add \"task description\"%n");
    }

    @Test
    void updatePrintsUsageWhenIdAndDescriptionAreMissing() {
        parser.parse(new String[] {"update"});

        assertOutput("Usage: update <id> \"new description\"%n");
    }

    @Test
    void updatePrintsUsageWhenDescriptionIsMissing() {
        parser.parse(new String[] {"update", "1"});

        assertOutput("Usage: update <id> \"new description\"%n");
    }

    @Test
    void updatePrintsInvalidTaskIdWhenIdIsNotANumber() {
        parser.parse(new String[] {"update", "abc", "New description"});
        assertOutput("Invalid task id: abc%n");
    }

    @Test
    void updatePrintsIdAndDescriptionWhenProvided() {
        parser.parse(new String[] {"update", "12", "New description"});

        assertOutput("Updating task 12: New description%n");
    }

    @Test
    void updatePrintsInvalidTaskIdWhenIdIsNegative() {
        parser.parse(new String[] {"update", "-1", "New description"});

        assertOutput("Invalid task id: -1%n");
    }

    @Test
    void updatePrintsUsageWhenExtraArgumentsAreProvided() {
        parser.parse(new String[] {"update", "12", "New description", "ignored"});

        assertOutput("Usage: update <id> \"new description\"%n");
    }

    @Test
    void deletePrintsUsageWhenIdIsMissing() {
        parser.parse(new String[] {"delete"});

        assertOutput("Usage: delete <id>%n");
    }

    @Test
    void deletePrintsInvalidTaskIdWhenIdIsNotANumber() {
        parser.parse(new String[] {"delete", "abc"});

        assertOutput("Invalid task id: abc%n");
    }

    @Test
    void deletePrintsIdWhenProvided() {
        parser.parse(new String[] {"delete", "8"});

        assertOutput("Deleting task: 8%n");
    }

    @Test
    void deletePrintsInvalidTaskIdWhenIdIsNegative() {
        parser.parse(new String[] {"delete", "-8"});

        assertOutput("Invalid task id: -8%n");
    }

    @Test
    void deletePrintsUsageWhenExtraArgumentsAreProvided() {
        parser.parse(new String[] {"delete", "8", "ignored"});

        assertOutput("Usage: delete <id>%n");
    }

    @Test
    void listPrintsListingMessage() {
        parser.parse(new String[] {"list"});

        assertOutput("Listing tasks%n");
    }

    @Test
    void listPrintsUsageWhenExtraArgumentsAreProvided() {
        parser.parse(new String[] {"list", "done"});

        assertOutput("Usage: list%n");
    }

    @Test
    void updateStatusPrintsUsageWhenArgumentsAreMissing() {
        parser.parse(new String[] {"update-status", "1"});

        assertOutput("Wrong number of arguments!%n");
    }

    @Test
    void updateStatusPrintsInvalidTaskIdWhenIdIsNotANumber() {
        parser.parse(new String[] {"update-status", "abc", "completed"});

        assertOutput("Invalid task id: abc%n");
    }

    @Test
    void updateStatusPrintsInvalidTaskIdWhenIdIsNotPositive() {
        parser.parse(new String[] {"update-status", "0", "completed"});

        assertOutput("Invalid task id: 0%n");
    }

    @Test
    void updateStatusPrintsInvalidStatusWhenStatusIsUnknown() {
        parser.parse(new String[] {"update-status", "1", "blocked"});

        assertOutput("Invalid task status: blocked%n");
    }

    @Test
    void updateStatusUpdatesTaskWithCaseInsensitiveStatus() {
        RecordingTaskService taskService = new RecordingTaskService();
        parser.taskService = taskService;

        parser.parse(new String[] {"update-status", "12", "In_Progress"});

        assertEquals(12, taskService.updatedTaskId);
        assertEquals(TaskStatus.IN_PROGRESS, taskService.updatedStatus);
        assertOutput("Updating task 12 with status IN_PROGRESS%n");
    }

    private void assertOutput(String expected) {
        assertEquals(String.format(expected), outputStream.toString(StandardCharsets.UTF_8));
    }

    private static final class RecordingTaskService extends TaskService {
        private int updatedTaskId;
        private TaskStatus updatedStatus;

        @Override
        public Task updateStatus(int id, TaskStatus status) {
            updatedTaskId = id;
            updatedStatus = status;
            return null;
        }
    }
}
