package com.tasktracker;

import com.tasktracker.cli.CommandParser;

public class Main {
    public static void main(String[] args) {
        CommandParser commandParser = new CommandParser();
        commandParser.parse(args);
    }

}
