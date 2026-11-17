package ru.edu.pr08;

public class ClientParseException extends Exception {
    private final int lineNumber;

    public ClientParseException(int lineNumber, String message) {
        super(message);
        this.lineNumber = lineNumber;
    }

    public int lineNumber() {
        return lineNumber;
    }
}
