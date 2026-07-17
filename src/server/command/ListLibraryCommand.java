package server.command;

import common.Request;
import common.Result;
import server.model.BookLibraryStatus;
import server.model.User;
import server.progress.ProgressService;
import server.session.SessionManager;

public class ListLibraryCommand extends AuthorizedCommand {

    private final ProgressService progressService;
    private final BookLibraryStatus.BookStatus status;

    public ListLibraryCommand(ProgressService progressService,
                              SessionManager sessionManager,
                              BookLibraryStatus.BookStatus status) {
        super(sessionManager);
        this.progressService = progressService;
        this.status = status;
    }

    @Override
    public Result<?> execute(Request request) {
        User user = getUser(request);

        if (status == BookLibraryStatus.BookStatus.NOT_READ) {
            return progressService.listNotRead(user);
        }
        if (status == BookLibraryStatus.BookStatus.READING) {
            return progressService.listReading(user);
        }
        return progressService.listRead(user);
    }
}
