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

    public static String writeResult(Result<?> result) {

        StringBuilder json = new StringBuilder();

        json.append("{");

        json.append("\"status\":\"").append(result.isSuccess() ? "success" : "error").append("\",");

        json.append("\"message\":\"").append(escape(result.getMessage())).append("\",");

        json.append("\"data\":");

        appendValue(json, result.getData());

        json.append("}");

        return json.toString();
    }

    private static void appendValue(StringBuilder json, Object value) {

        if (value == null) {
            json.append("null");
            return;
        }

        if (value instanceof Number || value instanceof Boolean) {

            json.append(value);
            return;
        }

        if (value instanceof Iterable<?> iterable) {

            json.append("[");

            boolean first = true;

            for (Object item : iterable) {

                if (!first) {
                    json.append(",");
                }

                appendValue(json, item);
                first = false;
            }

            json.append("]");
            return;
        }

        json.append("\"").append(escape(value.toString())).append("\"");
    }

    private static String escape(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
