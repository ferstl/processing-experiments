package com.github.ferstl.processing;

import java.util.UUID;

public class AccountingResult {

  private final UUID correlationId;
  private final AccountingStatus accountingStatus;

  public AccountingResult(UUID correlationId, AccountingStatus accountingStatus) {
    this.correlationId = correlationId;
    this.accountingStatus = accountingStatus;
  }
}
