package com.github.ferstl.processing;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class AccountMasterdata {

  private static final int NR_OF_ACCOUNTS = 300;
  private static final String[] REGULAR_ACCOUNTS;

  private static final String SUPER_ACCOUNT = "99999";

  static {
    Random random = new Random(42);
    REGULAR_ACCOUNTS = random.ints(50000, 90000)
        .limit(NR_OF_ACCOUNTS)
        .distinct()
        .mapToObj(Integer::toString)
        .toArray(String[]::new);

    if (REGULAR_ACCOUNTS.length != NR_OF_ACCOUNTS) {
      throw new IllegalStateException("The number of accounts does not match the expected value of " + NR_OF_ACCOUNTS);
    }
  }

  public static List<String> getRegularAccounts() {
    return List.of(REGULAR_ACCOUNTS);
  }

  public static String[] getRegularAccountsAsArray() {
    return Arrays.copyOf(REGULAR_ACCOUNTS, REGULAR_ACCOUNTS.length);
  }

  public static String getSuperAccount() {
    return SUPER_ACCOUNT;
  }
}
