package server.model;

import java.io.Serializable;

public class Wallet implements Serializable {
    private double balance;

    public  Wallet() {
        this.balance = 0;
    }

    public double getBalance() {
        return balance;
    }

    public synchronized void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        else {
            this.balance += amount;
        }
    }

    public synchronized void withdraw(double amount) {
        if (!hasEnough(amount)) {
            throw new IllegalArgumentException("Not enough enough money");
        }
        balance -= amount;
    }

    public boolean hasEnough(double amount) {
        return balance >= amount && amount > 0;
    }
}
