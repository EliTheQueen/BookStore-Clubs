package common;

public class Response {

    public static String success(String message) {
        return "{\"status\":\"success\",\"message\":\"" + message + "\"}";
    }

    public static String error(String message) {
        return "{\"status\":\"error\",\"message\":\"" + message + "\"}";
    }
}
