package ru.edu.pr08;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

public class SelfCheck {
    private static final String HEADER = "inn;name;email";
    private static int failed = 0;

    public static void main(String[] args) {
        try {
            checkSample();
            checkTypicalValue();
            checkInnLowerBound();
            checkInnUpperBound();
            checkNameLimit();
            checkEmptyValues();
            checkWrongFormat();
            checkDuplicate();
            checkLoopContinues();
            checkMissingFile();
        } catch (Exception e) {
            failed++;
            System.out.println("FAIL exception " + e);
        }
        System.out.println(failed == 0 ? "PASS" : "FAIL  failed checks: " + failed);
    }

    private static ImportResult run(ClientRepository repo, String... rows) throws IOException {
        Path file = Files.createTempFile("clients", ".csv");
        try {
            List<String> lines = new java.util.ArrayList<>();
            lines.add(HEADER);
            lines.addAll(List.of(rows));
            Files.write(file, lines, StandardCharsets.UTF_8);
            return new ClientImporter(repo).importFile(file);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void check(String title, boolean condition) {
        if (!condition) {
            failed++;
        }
        System.out.println((condition ? "ok   " : "FAIL ") + title);
    }

    private static void checkSample() throws IOException {
        Path sample = Path.of("data", "sample.csv");
        if (!Files.exists(sample)) {
            System.out.println("skip data/sample.csv not found");
            return;
        }
        ClientRepository repo = new ClientRepository();
        ImportResult r = new ClientImporter(repo).importFile(sample);
        check("sample.csv: принято 2, ошибок 2", r.accepted() == 2 && r.errors().size() == 2);
        check("sample.csv: ошибки в строках 3 и 5",
                r.errors().get(0).lineNumber() == 3 && r.errors().get(1).lineNumber() == 5);
        check("sample.csv: клиент сохранён в репозитории", repo.findByInn("7701234567") != null);
    }

    private static void checkTypicalValue() throws IOException {
        ClientRepository repo = new ClientRepository();
        ImportResult r = run(repo, "7701234567;Alpha LLC;office@alpha.ru");
        check("типовое значение принято", r.accepted() == 1 && r.errors().isEmpty());
        Client c = repo.findByInn("7701234567");
        check("типовое значение сохранено без искажений",
                c != null && c.name().equals("Alpha LLC") && c.email().equals("office@alpha.ru"));
    }

    private static void checkInnLowerBound() throws IOException {
        ImportResult ok = run(new ClientRepository(), "1234567890;A;a@b");
        ImportResult bad = run(new ClientRepository(), "123456789;A;a@b");
        check("ИНН из 10 цифр принят", ok.accepted() == 1);
        check("ИНН из 9 цифр отклонён", bad.accepted() == 0 && bad.errors().size() == 1);
    }

    private static void checkInnUpperBound() throws IOException {
        ImportResult ok = run(new ClientRepository(), "123456789012;A;a@b");
        ImportResult bad = run(new ClientRepository(), "1234567890123;A;a@b");
        ImportResult mid = run(new ClientRepository(), "12345678901;A;a@b");
        check("ИНН из 12 цифр принят", ok.accepted() == 1);
        check("ИНН из 13 цифр отклонён", bad.accepted() == 0);
        check("ИНН из 11 цифр отклонён", mid.accepted() == 0);
    }

    private static void checkNameLimit() throws IOException {
        String atLimit = "N".repeat(ClientValidator.MAX_NAME_LENGTH);
        String overLimit = "N".repeat(ClientValidator.MAX_NAME_LENGTH + 1);
        ImportResult ok = run(new ClientRepository(), "1234567890;" + atLimit + ";a@b");
        ImportResult bad = run(new ClientRepository(), "1234567890;" + overLimit + ";a@b");
        check("имя из 15 символов принято", ok.accepted() == 1);
        check("имя из 16 символов отклонено", bad.accepted() == 0 && bad.errors().size() == 1);
    }

    private static void checkEmptyValues() throws IOException {
        ImportResult r = run(new ClientRepository(),
                ";A;a@b",
                "1234567890;;a@b",
                "1234567890;A;",
                "1234567890;A",
                "");
        check("пустые и отсутствующие значения отклонены", r.accepted() == 0 && r.errors().size() == 5);
        check("номера строк ошибок 2..6", r.errors().get(0).lineNumber() == 2 && r.errors().get(4).lineNumber() == 6);
    }

    private static void checkWrongFormat() throws IOException {
        ImportResult r = run(new ClientRepository(),
                "12345abcde;A;a@b",
                "1234567890;A;ab",
                "1234567890;A;a@@b",
                "1234567890;A;@b",
                "1234567890;A;a@",
                "1234567890;A;a@b;extra");
        check("неверный формат ИНН и email отклонён", r.accepted() == 0 && r.errors().size() == 6);
    }

    private static void checkDuplicate() throws IOException {
        ClientRepository repo = new ClientRepository();
        ImportResult r = run(repo,
                "1234567890;First;a@b",
                "1234567890;Second;c@d");
        check("дубликат ИНН: принят 1, ошибка 1", r.accepted() == 1 && r.errors().size() == 1);
        check("дубликат ИНН: сохранена первая запись", repo.findByInn("1234567890").name().equals("First"));
    }

    private static void checkLoopContinues() throws IOException {
        ImportResult r = run(new ClientRepository(),
                "1234567890;A;a@b",
                "bad line",
                "1234567891;B;b@c");
        check("ошибка строки не прерывает импорт", r.accepted() == 2 && r.errors().size() == 1);
    }

    private static void checkMissingFile() {
        boolean thrown = false;
        try {
            new ClientImporter(new ClientRepository()).importFile(Path.of("no_such_dir", "no_such_file.csv"));
        } catch (NoSuchFileException e) {
            thrown = true;
        } catch (IOException e) {
            thrown = true;
        }
        check("отсутствующий файл завершает импорт IOException", thrown);
    }
}
