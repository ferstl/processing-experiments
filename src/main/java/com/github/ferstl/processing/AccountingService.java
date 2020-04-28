package com.github.ferstl.processing;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import static java.util.stream.Collectors.toMap;

public class AccountingService {

  private final Map<String, BigDecimal> accountBook;

  public AccountingService() {
    this.accountBook = AccountMasterdata.getRegularAccounts().stream()
        .collect(toMap(
            key -> key,
            value -> new BigDecimal("100000000.00"),
            (val1, val2) -> {
              throw new RuntimeException("Duplicate keys should never happen " + val1 + ", " + val2);
            },
            HashMap::new));

    BigDecimal allBalances = getTotalBalance();
    this.accountBook.put(AccountMasterdata.getSuperAccount(), allBalances.negate());

    if (BigDecimal.ZERO.compareTo(getTotalBalance()) != 0) {
      throw new IllegalStateException("Corrupt account book. Sum of all accounts is expected to be 0.00 but is " + getTotalBalance());
    }
  }

  public void transfer(String debtor, String creditor, BigDecimal amount) {
    this.accountBook.put(debtor, this.accountBook.get(debtor).subtract(amount));
    this.accountBook.put(creditor, this.accountBook.get(creditor).add(amount));
  }

  public BigDecimal getTotalBalance() {
    BigDecimal totalBalance = BigDecimal.ZERO;
    for (BigDecimal balance : this.accountBook.values()) {
      totalBalance = totalBalance.add(balance);
    }

    return totalBalance;
  }
}
