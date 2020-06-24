package com.github.ferstl.processing.event.codec;

import java.util.UUID;
import com.github.ferstl.processing.accounting.AccountingStatus;

public class CommunicationEvent extends ProcessingEvent {

  private final AccountingStatus accountingStatus;

  public CommunicationEvent(UUID correlationId, AccountingStatus accountingStatus) {
    super(correlationId);
    this.accountingStatus = accountingStatus;
  }

  public AccountingStatus getAccountingStatus() {
    return this.accountingStatus;
  }
}
