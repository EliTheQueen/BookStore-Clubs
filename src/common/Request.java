package common;

import java.util.HashMap;
import java.util.Map;

public class Request {
    private String command;
    private String token;
    private Map<String, String> payload;

    public Request() {
        payload = new HashMap<>();
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Map<String, String> getPayload() {
        return payload;
    }

    public void put(String key, String value) {
        payload.put(key, value);
    }

    public String get(String key) {
        return payload.get(key);
    }
}
