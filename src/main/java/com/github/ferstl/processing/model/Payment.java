package com.github.ferstl.processing.model;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Payment {

  @XmlElement
  private final String debtorAccount;
  @XmlElement
  private final String creditorAccount;
  @XmlElement
  private final BigDecimal amount;

  Payment() {
    this.debtorAccount = null;
    this.creditorAccount = null;
    this.amount = null;
  }

  public Payment(String debtorAccount, String creditorAccount, BigDecimal amount) {
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
