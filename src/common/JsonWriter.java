package common;

import java.io.IOException;
import java.util.Map;

public class JsonWriter {

    public static String write(Request request) {
        //StringBuilder همان حافظه را تغییر می‌دهد و سریع‌تر است.
        StringBuilder json = new StringBuilder();

        json.append("{");

        json.append("\"command\":\"").append(request.getCommand()).append("\"");

        //چون Register هنوز Token ندارد.
        if (request.getToken() != null) {

            json.append(",");

            json.append("\"token\":\"").append(request.getToken()).append("\"");
        }

        json.append(",");

        json.append("\"payload\":{");

        //اولین عضو نباید قبلش کاما داشته باشد.
        boolean first = true;

        for (Map.Entry<String, String> entry : request.getPayload().entrySet()) {
            if (!first) {
                json.append(",");
            }

            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }

        json.append("}");
        json.append("}");

        return json.toString();
    }
}
