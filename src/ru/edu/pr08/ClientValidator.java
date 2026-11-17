package ru.edu.pr08;

import java.util.ArrayList;
import java.util.List;

public class ClientValidator {
    public static final int MAX_NAME_LENGTH = 15;

    public List<String> validate(ClientDraft draft) {
        List<String> errors = new ArrayList<>();
        validateInn(draft.inn(), errors);
        validateName(draft.name(), errors);
        validateEmail(draft.email(), errors);
        return errors;
    }

    private void validateInn(String inn, List<String> errors) {
        if (inn == null || inn.isEmpty()) {
            errors.add("ИНН пустой");
            return;
        }
        if (!isAsciiDigits(inn)) {
            errors.add("ИНН должен содержать только цифры");
            return;
        }
        if (inn.length() != 10 && inn.length() != 12) {
            errors.add("ИНН должен содержать 10 или 12 цифр, получено: " + inn.length());
        }
    }

    private void validateName(String name, List<String> errors) {
        if (name == null || name.isEmpty()) {
            errors.add("имя пустое");
            return;
        }
        if (name.length() > MAX_NAME_LENGTH) {
            errors.add("имя длиннее " + MAX_NAME_LENGTH + " символов: " + name.length());
        }
    }

    private void validateEmail(String email, List<String> errors) {
        if (email == null || email.isEmpty()) {
            errors.add("email пустой");
            return;
        }
        int first = email.indexOf('@');
        if (first < 0 || first != email.lastIndexOf('@')) {
            errors.add("email должен содержать ровно один символ @");
            return;
        }
        if (first == 0) {
            errors.add("email: пустая часть слева от @");
        }
        if (first == email.length() - 1) {
            errors.add("email: пустая часть справа от @");
        }
    }

    private boolean isAsciiDigits(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
