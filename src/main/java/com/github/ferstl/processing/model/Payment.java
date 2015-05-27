package com.github.ferstl.processing.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Payment {

  private String from;
  private String to;
  private BigDecimal amount;
  private LocalDate valueDate;

  public String getFrom() {
    return this.from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return this.to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public BigDecimal getAmount() {
    return this.amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public LocalDate getValueDate() {
    return this.valueDate;
  }

  public void setValueDate(LocalDate valueDate) {
    this.valueDate = valueDate;
  }


}
