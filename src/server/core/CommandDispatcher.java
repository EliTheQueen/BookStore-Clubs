package server.core;

import common.Request;
import common.Result;
import server.auth.AuthService;
import server.book.BookService;
import server.club.ClubService;
import server.command.*;
import server.fundraiser.FundraiserService;
import server.lending.LendingService;
import server.model.BookLibraryStatus;
import server.notif.NotificationService;
import server.progress.ProgressService;
import server.session.Session;
import server.session.SessionManager;
import server.wallet.WalletService;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class CommandDispatcher {

    private final Map<String, CommandHandler> commands;
    private final SessionManager sessionManager;
    private final NotificationService notificationService;

    public CommandDispatcher(
            AuthService authService,
            BookService bookService,
            ProgressService progressService,
            ClubService clubService,
            FundraiserService fundraiserService,
            LendingService lendingService,
            WalletService walletService,
            SessionManager sessionManager,
            NotificationService notificationService) {

        commands = new HashMap<>();
        this.sessionManager = sessionManager;
        this.notificationService = notificationService;

        commands.put("register", new RegisterCommand(authService));

        commands.put("login", new LoginCommand(authService));

        commands.put("logout", new LogoutCommand(authService));

        commands.put("books_market_list", new BooksMarketCommand(bookService, sessionManager));
        commands.put("list_market_books", new BooksMarketCommand(bookService, sessionManager));

        commands.put("book_buy", new BookBuyCommand(bookService, sessionManager));
        commands.put("buy_book", new BookBuyCommand(bookService, sessionManager));

        commands.put("account_charge", new ChargeCommand(walletService, sessionManager));
        commands.put("charge_account", new ChargeCommand(walletService, sessionManager));

        commands.put("balance_show", new BalanceCommand(walletService, sessionManager));
        commands.put("show_balance", new BalanceCommand(walletService, sessionManager));

        commands.put("submit_progress", new SubmitProgressCommand(progressService, sessionManager));

        commands.put("list_not_read_library", new ListLibraryCommand(
                progressService, sessionManager, BookLibraryStatus.BookStatus.NOT_READ));
        commands.put("list_reading_library", new ListLibraryCommand(
                progressService, sessionManager, BookLibraryStatus.BookStatus.READING));
        commands.put("list_read_library", new ListLibraryCommand(
                progressService, sessionManager, BookLibraryStatus.BookStatus.READ));

        commands.put("create_club", new CreateClubCommand(clubService, sessionManager));
        commands.put("list_clubs", new ListClubsCommand(clubService, sessionManager, false));
        commands.put("total_list_clubs", new ListClubsCommand(clubService, sessionManager, true));
        commands.put("join", new JoinClubCommand(clubService, sessionManager));
        commands.put("view_club", new ViewClubCommand(clubService, sessionManager));
        commands.put("accept_join_request", new AnswerJoinRequestCommand(clubService, sessionManager, true));
        commands.put("deny_join_request", new AnswerJoinRequestCommand(clubService, sessionManager, false));
        commands.put("list_club_members", new ListClubMembersCommand(clubService, sessionManager));
        commands.put("remove_member", new RemoveMemberCommand(clubService, sessionManager));
        commands.put("create_fundraiser", new CreateFundraiserCommand(fundraiserService, sessionManager));
        commands.put("view_fundraiser_progress", new ViewFundraiserProgressCommand(fundraiserService, sessionManager));
        commands.put("donate", new DonateCommand(fundraiserService, sessionManager));
        commands.put("add_comment", new AddCommentCommand(clubService, sessionManager));
        commands.put("list_comments", new ListCommentsCommand(clubService, sessionManager));
        commands.put("lend_book", new LendBookCommand(lendingService, sessionManager));
    }

    public Result<?> dispatch(Request request) {

        if (request == null) {
            return Result.error("Invalid request.");
        }

        String command = request.getCommand();

        if (command == null || command.isBlank()) {
            return Result.error("Command is missing.");
        }

        CommandHandler commandHandler = commands.get(command);

        if (commandHandler == null) {
            return Result.error("Unknown command.");
        }

        return commandHandler.execute(request);
    }

    public void registerOnlineClient(Request request, PrintWriter writer) {
        if (request == null) {
            return;
        }
        Session session = sessionManager.getSession(request.getToken());
        if (session != null) {
            notificationService.register(session.getUser().getUsername(), writer);
        }
    }
}
