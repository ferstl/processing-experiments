package com.github.ferstl.processing.event.codec;

import java.util.UUID;

public class Reservation extends ProcessingEvent {

  private final int debtorAccount;
  private final int creditorAccount;
  private final long amount;

  public Reservation(UUID correlationId, int debtorAccount, int creditorAccount, long amount) {
    super(correlationId);
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
