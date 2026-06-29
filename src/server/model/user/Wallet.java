package server.model.user;

public class Wallet {
    private double balance;

    public void addMoney(double money) {
        this.balance += money;
    }

    public void withdraw(double money) {
        if (balance >= money) {
            this.balance -= money;
        }
        else  {
            System.out.println("Insufficient balance");
        }
    }
}
