package com.github.ferstl.processing.accounting;

import java.util.UUID;

public class AccountingResult {

  private final UUID correlationId;
  private final AccountingStatus accountingStatus;

  public AccountingResult(UUID correlationId, AccountingStatus accountingStatus) {
    this.correlationId = correlationId;
    this.accountingStatus = accountingStatus;
  }

  public UUID getCorrelationId() {
    return this.correlationId;
  }

  public AccountingStatus getAccountingStatus() {
    return this.accountingStatus;
  }
}
