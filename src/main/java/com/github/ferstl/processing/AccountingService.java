package com.github.ferstl.processing;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import static java.util.stream.Collectors.toMap;

public class AccountingService {

  private final Map<String, BigDecimal> accountBook;

  public AccountingService() {
    this.accountBook = AccountMasterdata.getAccounts().stream()
        .collect(toMap(
            key -> key,
            value -> new BigDecimal("100000000.00"),
            (val1, val2) -> {
              throw new RuntimeException("Duplicate keys should never happen " + val1 + ", " + val2);
            },
            HashMap::new));
  }

  public void transfer(String debtor, String creditor, BigDecimal amount) {
    this.accountBook.put(debtor, this.accountBook.get(debtor).subtract(amount));
    this.accountBook.put(debtor, this.accountBook.get(creditor).add(amount));
  }
}
