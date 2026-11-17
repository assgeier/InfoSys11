package ru.edu.pr08;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ClientImporter {
    private final ClientCsvParser parser = new ClientCsvParser();
    private final ClientValidator validator = new ClientValidator();
    private final ClientRepository repository;

    public ClientImporter(ClientRepository repository) {
        this.repository = repository;
    }

    public ImportResult importFile(Path path) throws IOException {
        ImportResult result = new ImportResult();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            reader.readLine();
            int lineNumber = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                processLine(line, lineNumber, result);
            }
        }
        return result;
    }

    private void processLine(String line, int lineNumber, ImportResult result) {
        ClientDraft draft;
        try {
            draft = parser.parse(line, lineNumber);
        } catch (ClientParseException e) {
            result.addError(new ImportError(lineNumber, e.getMessage()));
            return;
        }
        List<String> problems = validator.validate(draft);
        if (!problems.isEmpty()) {
            result.addError(new ImportError(lineNumber, String.join("; ", problems)));
            return;
        }
        if (repository.findByInn(draft.inn()) != null) {
            result.addError(new ImportError(lineNumber, "клиент с ИНН " + draft.inn() + " уже существует"));
            return;
        }
        repository.save(new Client(draft.inn(), draft.name(), draft.email()));
        result.addAccepted();
    }
}
