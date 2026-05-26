package com.tasktracker.cli;

public class CommandParser {

    public void parse(String[] args) {
        // validate empty input
        if (args.length == 0) {
            System.out.println("No command provided.");
            return;
        }

        // detect command
        String command = args[0];

        switch (command) {

            case "add":
//                handleAdd(args);
                break;

            case "update":
//                handleUpdate(args);
                break;

            case "delete":
//                handleDelete(args);
                break;

            case "list":
//                handleList(args);
                break;

            default:
                System.out.println("Unknown command.");
        }

    }
}
