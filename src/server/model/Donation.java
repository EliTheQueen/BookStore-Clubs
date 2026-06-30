package server.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Donation implements Serializable {
    private String username;
    private double amount;
    private LocalDateTime donatedAt;

    public  Donation(String username,double amount) {
        if(username == null || username.isBlank())
            throw new IllegalArgumentException("Username cannot be null or blank");

        if(amount <=0)
            throw new IllegalArgumentException("Amount cannot be negative");

        this.username = username;
        this.amount = amount;
        this.donatedAt = LocalDateTime.now();
    }

    public String getUsername() { return username; }
    public double getAmount() { return amount; }
    public LocalDateTime getDonatedAt() { return donatedAt; }

    public String toDisplayString() {
        return username + " donated " + amount + " at " + donatedAt;
    }
}
