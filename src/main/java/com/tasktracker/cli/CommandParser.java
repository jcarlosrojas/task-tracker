package com.tasktracker.cli;

import java.util.Locale;
import java.util.Objects;

public class CommandParser {

    public void parse(String[] args) {
        Objects.requireNonNull(args, "args is null");

        if (args.length == 0) {
            printHelp();
            return;
        }

        String command = args[0].toLowerCase(Locale.ROOT);

        switch (command) {
            case "add":
                handleAdd(args);
                break;
            case "update":
                handleUpdate(args);
                break;
            case "delete":
                handleDelete(args);
                break;
            case "list":
                handleList(args);
                break;
            default:
                System.out.println("Unknown command: " + command);
                printHelp();
        }
    }

    private void handleAdd(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: add \"task description\"");
            return;
        }

        String description = args[1].trim();
        if (description.isEmpty()) {
            System.out.println("Usage: add \"task description\"");
            return;
        }
        System.out.println("Adding task: " + description);

        // Later:
        //taskService.add(description);
    }

    private void handleUpdate(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: update <id> \"new description\"");
            return;
        }

        Integer id = parseId(args[1]);
        if (id == null) {
            return;
        }

        String description = args[2].trim();
        if (description.isEmpty()) {
            System.out.println("Usage: update <id> \"new description\"");
            return;
        }
        System.out.println("Updating task " + id + ": " + description);

        // Later:
        // taskService.update(id, description);
    }

    private void handleDelete(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: delete <id>");
            return;
        }

        Integer id = parseId(args[1]);
        if (id == null) {
            return;
        }

        System.out.println("Deleting task: " + id);

        // Later:
        // taskService.delete(id);
    }

    private void handleList(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: list");
            return;
        }

        System.out.println("Listing tasks");
        // Later:
        // taskService.list();
    }

    private Integer parseId(String value) {
        try {
            int id = Integer.parseInt(value);
            if (id < 1) {
                System.out.println("Invalid task id: " + value);
                return null;
            }
            return id;
        } catch (NumberFormatException exception) {
            System.out.println("Invalid task id: " + value);
            return null;
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  add \"task description\"");
        System.out.println("  update <id> \"new description\"");
        System.out.println("  delete <id>");
        System.out.println("  list");
    }
}