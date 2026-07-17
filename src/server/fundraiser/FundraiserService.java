package server.fundraiser;

import common.Result;
import server.book.BookService;
import server.club.ClubService;
import server.model.Book;
import server.model.Club;
import server.model.Donation;
import server.model.Fundraiser;
import server.model.User;
import server.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FundraiserService {

    private final ClubService clubService;
    private final BookService bookService;
    private final UserRepository userRepository;
    private final AtomicInteger nextFundraiserId;

    public FundraiserService(ClubService clubService, BookService bookService, UserRepository userRepository) {
        this.clubService = clubService;
        this.bookService = bookService;
        this.userRepository = userRepository;
        this.nextFundraiserId = new AtomicInteger(1);
    }

    public Result<Fundraiser> createFundraiser(User creator, int clubId, int bookId) {
        Club club = clubService.findClub(clubId);
        if (club == null) {
            return Result.error("Club not found.");
        }
        if (creator == null || !club.isMember(creator.getUsername())) {
            return Result.error("You are not a member of this club.");
        }

        Book book = bookService.findBookById(bookId);
        if (book == null) {
            return Result.error("Book not found.");
        }

        synchronized (club) {
            if (club.hasActiveFundraiser()) {
                Fundraiser active = club.getActiveFundraiser();
                if (active.getBookId() == bookId && !active.isComplete()) {
                    return Result.error("This book already has an active fundraiser in this club.");
                }
                if (!active.isComplete()) {
                    return Result.error("Club already has active fundraiser.");
                }
                club.clearActiveFundraiser();
            }

            int missingCount = 0;
            for (String username : club.getMembers()) {
                User member = userRepository.find(username);
                if (member != null && !member.hasBook(bookId)) {
                    missingCount++;
                }
            }

            double targetAmount = missingCount * book.getPrice();
            Fundraiser fundraiser = new Fundraiser(nextFundraiserId.getAndIncrement(), clubId, bookId, targetAmount);
            club.setActiveFundraiser(fundraiser);

            if (targetAmount == 0) {
                fundraiser.markCompleted();
                completeFundraiser(club, fundraiser, book);
            }

            return Result.success("Fundraiser created.", fundraiser);
        }
    }

    public Result<List<String>> viewProgress(User user, int clubId) {
        Club club = clubService.findClub(clubId);
        if (club == null) {
            return Result.error("Club not found.");
        }
        if (user == null || !club.isMember(user.getUsername())) {
            return Result.error("You are not a member of this club.");
        }

        Fundraiser fundraiser = club.getActiveFundraiser();
        if (fundraiser == null) {
            return Result.error("There is no active fundraiser.");
        }

        List<String> data = new ArrayList<>();
        data.add(fundraiser.toString());
        for (Donation donation : fundraiser.getDonations()) {
            data.add(donation.toDisplayString());
        }

        return Result.success("Fundraiser progress loaded.", data);
    }

    public Result<Void> donate(User user, int clubId, double amount) {
        Club club = clubService.findClub(clubId);
        if (club == null) {
            return Result.error("Club not found.");
        }
        if (user == null || !club.isMember(user.getUsername())) {
            return Result.error("You are not a member of this club.");
        }

        Fundraiser fundraiser = club.getActiveFundraiser();
        if (fundraiser == null) {
            return Result.error("There is no active fundraiser.");
        }

        Book book = bookService.findBookById(fundraiser.getBookId());
        if (book == null) {
            return Result.error("Book not found.");
        }

        synchronized (club) {
            try {
                double actual = fundraiser.donate(user.getUsername(), amount, user.getWallet());
                if (fundraiser.isComplete()) {
                    completeFundraiser(club, fundraiser, book);
                }
                return Result.success("Donation accepted. Actual donation: " + actual);
            } catch (RuntimeException exception) {
                return Result.error(exception.getMessage());
            }
        }
    }

    private void completeFundraiser(Club club, Fundraiser fundraiser, Book book) {
        for (String username : club.getMembers()) {
            User member = userRepository.find(username);
            if (member != null && !member.hasBook(book.getID())) {
                member.addBook(book);
            }
        }
        club.clearActiveFundraiser();
    }
}
