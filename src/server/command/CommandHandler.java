package server.command;

import common.Request;
import common.Result;

public interface CommandHandler {

    Result<?> execute(Request request);

}
