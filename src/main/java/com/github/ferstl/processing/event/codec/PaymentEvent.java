package com.github.ferstl.processing.event.codec;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentEvent extends ProcessingEvent {

  private final String debtorAccount;
  private final String creditorAccount;
  private final BigDecimal amount;

  public PaymentEvent(UUID correlationId, String debtorAccount, String creditorAccount, BigDecimal amount) {
    super(correlationId);
    this.debtorAccount = debtorAccount;
    this.creditorAccount = creditorAccount;
    this.amount = amount;
  }

  public String getDebtorAccount() {
    return this.debtorAccount;
  }

  public String getCreditorAccount() {
    return this.creditorAccount;
  }

  public BigDecimal getAmount() {
    return this.amount;
  }
}
