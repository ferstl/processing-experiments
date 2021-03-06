package com.github.ferstl.processing.accounting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.github.ferstl.processing.event.codec.Reservation;
import static com.github.ferstl.processing.accounting.AccountingStatus.RESERVATION_NOT_FOUND;
import static com.github.ferstl.processing.accounting.AccountingStatus.SETTLED;
import static java.util.stream.Collectors.toMap;

public class AccountingService {

  private final Map<Integer, Long> accountBook;
  private final Map<UUID, Reservation> reservations;

  public AccountingService() {
    this.reservations = new HashMap<>();
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

  public AccountingResult reserve(Reservation reservation) {
    Long totalAmount = this.accountBook.get(reservation.getDebtorAccount());
    if (totalAmount == null) {
      System.out.println("debtor: " + reservation.getDebtorAccount());
      return new AccountingResult(reservation.getCorrelationId(), AccountingStatus.UNKNOWN_ACCOUNT);
    }

    long newAmount = totalAmount - reservation.getAmount();
    if (newAmount >= 0) {
      this.accountBook.put(reservation.getDebtorAccount(), newAmount);
      this.reservations.put(reservation.getCorrelationId(), reservation);
      return new AccountingResult(reservation.getCorrelationId(), AccountingStatus.RESERVATION_OK);
    } else {
      return new AccountingResult(reservation.getCorrelationId(), AccountingStatus.INSUFFICIENT_FUNDS);
    }
  }

  public AccountingResult settle(UUID correlationId) {
    Reservation reservation = this.reservations.get(correlationId);
    if (reservation == null) {
      return new AccountingResult(correlationId, RESERVATION_NOT_FOUND);
    }

    int creditorAccount = reservation.getCreditorAccount();
    Long creditorAmount = this.accountBook.get(creditorAccount);
    this.accountBook.put(creditorAccount, creditorAmount + reservation.getAmount());

    return new AccountingResult(correlationId, SETTLED);
  }

  public long getTotalBalance() {
    return this.accountBook.values().stream()
        .mapToLong(Long::longValue)
        .sum();
  }
}
