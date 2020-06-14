package com.github.ferstl.processing;

import java.util.HashMap;
import java.util.Map;
import static java.util.stream.Collectors.toMap;

public class AccountingService {

  private final Map<Integer, Long> accountBook;

  public AccountingService() {
    this.accountBook = AccountMasterdata.getRegularAccounts().stream()
        .collect(toMap(
            key -> key,
            value -> 100000000L,
            (val1, val2) -> {
              throw new RuntimeException("Duplicate keys should never happen " + val1 + ", " + val2);
            },
            HashMap::new));

    long totalBalance = getTotalBalance();
    this.accountBook.put(AccountMasterdata.getSuperAccount(), totalBalance * -1);

    if (getTotalBalance() != 0) {
      throw new IllegalStateException("Corrupt account book. Sum of all accounts is expected to be 0.00 but is " + getTotalBalance());
    }
  }

  public void transfer(int debtor, int creditor, long amount) {
    this.accountBook.put(debtor, this.accountBook.get(debtor) - amount);
    this.accountBook.put(creditor, this.accountBook.get(creditor) + amount);
  }

  public long getTotalBalance() {
    return this.accountBook.values().stream()
        .mapToLong(Long::longValue)
        .sum();
  }
}
