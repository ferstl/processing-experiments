package com.github.ferstl.processing;

public class Reservation {

  private final int debtorAccount;
  private final int creditorAccount;
  private final long amount;

  public Reservation(int debtorAccount, int creditorAccount, long amount) {
    this.debtorAccount = debtorAccount;
    this.creditorAccount = creditorAccount;
    this.amount = amount;
  }

  public int getDebtorAccount() {
    return this.debtorAccount;
  }

  public int getCreditorAccount() {
    return this.creditorAccount;
  }

  public long getAmount() {
    return this.amount;
  }
}
