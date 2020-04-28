package com.github.ferstl.processing;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class AccountMasterdata {

  private static final int NR_OF_ACCOUNTS = 300;
  private static final String[] ACCOUNTS;

  static {
    Random random = new Random(42);
    ACCOUNTS = random.ints(50000, 70000)
        .limit(NR_OF_ACCOUNTS)
        .mapToObj(Integer::toString)
        .toArray(String[]::new);
  }

  public static List<String> getAccounts() {
    return List.of(ACCOUNTS);
  }

  public static String[] getAccountsAsArray() {
    return Arrays.copyOf(ACCOUNTS, ACCOUNTS.length);
  }
}
