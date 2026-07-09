package server.core;

import common.Response;

public class CommandDispatcher {

    public String dispatch(String request) {

        if (request == null || request.isBlank()) {
            return Response.error("empty request");
        }

        if (request.contains("\"command\":\"ping\"")) {
            return Response.success("pong");
        }

        return Response.error("unknown command");
    }
}