package Aufgabe;

import java.util.*;

public class ErrorReporter {
    private final List<String> errors = new ArrayList<>();

    public void error(String msg) {
        errors.add(msg);
        System.err.println("[ERROR] " + msg);
    }

    public boolean hasErrors() { return !errors.isEmpty(); }

    public List<String> getErrors() { return List.copyOf(errors); }

    public void printSummary() {
        if (errors.isEmpty()) {
            System.out.println("No semantic errors found.");
        } else {
            System.out.println("Semantic errors:");
            for (String e : errors) System.out.println(" - " + e);
        }
    }
}
