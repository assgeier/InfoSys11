package ru.edu.pr08;

public class ClientCsvParser {
    public static final String DELIMITER = ";";
    public static final int FIELD_COUNT = 3;

    public ClientDraft parse(String line, int lineNumber) throws ClientParseException {
        if (line == null || line.isBlank()) {
            throw new ClientParseException(lineNumber, "пустая строка");
        }
        String[] parts = line.split(DELIMITER, -1);
        if (parts.length != FIELD_COUNT) {
            throw new ClientParseException(lineNumber,
                    "ожидалось полей: " + FIELD_COUNT + ", получено: " + parts.length);
        }
        return new ClientDraft(parts[0].trim(), parts[1].trim(), parts[2].trim());
    }
}
