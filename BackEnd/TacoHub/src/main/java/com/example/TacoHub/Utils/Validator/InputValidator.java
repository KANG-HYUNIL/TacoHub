package com.example.TacoHub.Utils.Validator;

import java.util.regex.Pattern;

public class InputValidator {

    private static final String VALID_ID_CHARACTERS = "^[a-zA-Z0-9._-]+$";
    private static final String VALID_PASSWORD_CHARACTERS = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]*$";

    //id 유효성 검증
    //유효한 id면 true, 아니면 false
    // ID validation
    public static boolean isValid(String input) {
        return Pattern.matches(VALID_ID_CHARACTERS, input);
    }
    //password 유효성 검증
    //유효한 password면 true, 아니면 false
    public static boolean isPasswordValid(String password) {
        return Pattern.matches(VALID_PASSWORD_CHARACTERS, password);
    }
}
