import common.JsonWriter;
import common.Request;

public class Test {
    public static void main(String[] args) {
        Request request = new Request();

        request.setCommand("login");

        request.setToken("abc123");

        request.put("username", "ali");
        request.put("password", "123");

        String json = JsonWriter.write(request);

        System.out.println(json);
    }
}
