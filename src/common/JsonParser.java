package common;

public class JsonParser {

    public static Request parse(String json) {

        Request request = new Request();

        json = json.trim();

        json = json.substring(1, json.length() - 1);

        int payloadIndex = json.indexOf("\"payload\"");

        String header = json.substring(0, payloadIndex);

        String payload = json.substring(payloadIndex);

        parseHeader(header, request);

        parsePayload(payload, request);

        return request;

    }

    private static void parseHeader(String header, Request request) {

        header = header.replace("\"", "");

        String[] fields = header.split(",");

        for (String field : fields) {

            if (field.isBlank())
                continue;

            String[] pair = field.split(":");

            if (pair.length != 2)
                continue;

            String key = pair[0].trim();

            String value = pair[1].trim();

            switch (key) {

                case "command":
                    request.setCommand(value);
                    break;

                case "token":
                    request.setToken(value);
                    break;
            }

        }

    }

    private static void parsePayload(String payload, Request request) {

        int start = payload.indexOf("{");

        int end = payload.lastIndexOf("}");

        payload = payload.substring(start + 1, end);

        if (payload.isBlank())
            return;

        String[] fields = payload.split(",");

        for (String field : fields) {

            String[] pair = field.split(":");

            if (pair.length != 2)
                continue;

            String key = pair[0].replace("\"", "").trim();

            String value =
                    pair[1].replace("\"", "").trim();

            request.put(key, value);

        }

    }

}