package chat.network;

public interface IStatusCodes {
    String EXISTING_LOGIN = "ALREADY_EXISTING_LOGIN";
    String NONEXISTENT_LOGIN = "NONEXISTENT_LOGIN";
    String WRONG_PASSWORD = "WRONG_PASSWORD";
    String REGISTRATION_TOKEN = "REGISTRATION_TOKEN";
    String REGISTRATION_SUCCESS_TOKEN = "SUCCESS_REGISTRATION";
    String LOGIN_TOKEN = "LOGIN_TOKEN";
    String TO_CHAT = "TO_CHAT";
    String DUAL_CONNECTION_BLOCK = "DUAL_CONNECTION_BLOCK";
    String SPACES_ERROR = "NO_SPACES_IN_LOGIN_AND_PASSWORD";
}
