package server.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Fundraiser implements Serializable {
    private int id;
    private int clubId;
    private int bookId;
    private double targetAmount;
    private double currentAmount;
    private FundraiserStatus.fundraiserStatus status;
    private List<Donation> donations = new ArrayList<>();

    public  Fundraiser(int id, int clubId, int bookId, double targetAmount) {
        if (clubId < 0 || bookId < 0 || bookId > 101 || targetAmount < 0) {
            throw new IllegalArgumentException("numbers are not valid");
        }
        this.id = id;
        this.clubId = clubId;
        this.bookId = bookId;
        this.targetAmount = targetAmount;
        this.currentAmount = 0;
        this.status = FundraiserStatus.fundraiserStatus.ACTIVE;
    }

    public synchronized double donate(String username, int amount, Wallet wallet) {
        if (!status.equals(FundraiserStatus.fundraiserStatus.ACTIVE)) {
            throw new IllegalArgumentException("Fundraiser is not active");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount is not valid");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is not valid");
        }

        if (wallet == null) {
            throw new IllegalArgumentException("Wallet cannot be null");
        }

        double remaining = remainingAmount();
        double actualDonation = Math.min(amount, remaining);

        if (!wallet.hasEnough(actualDonation)) {
            throw new IllegalStateException("Wallet balance is not enough");
        }
        wallet.withdraw(actualDonation);

        currentAmount += actualDonation;

        Donation donation = new Donation(username, actualDonation);
        donations.add(donation);

        if (currentAmount >= targetAmount) {
            complete();
        }

        return actualDonation;
    }

    public synchronized double remainingAmount() {
        return targetAmount - currentAmount;
    }

    public boolean isComplete() {
        if (status == FundraiserStatus.fundraiserStatus.COMPLETED) {
            return true;
        }
        return false;
    }

    private void complete() {
        this.status = FundraiserStatus.fundraiserStatus.COMPLETED;
    }

    public int getId() {
        return id;
    }
    public int getClubId() {
        return clubId;
    }
    public int getBookId() {
        return bookId;
    }
    public double getTargetAmount() {
        return targetAmount;
    }
    public double getCurrentAmount() {
        return currentAmount;
    }
    public List<Donation> getDonations() {
        return new ArrayList<>(donations);
    }
}
