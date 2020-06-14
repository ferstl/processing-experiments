package com.github.ferstl.processing;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import static java.util.stream.Collectors.toList;

public final class AccountMasterdata {

  private static final int NR_OF_ACCOUNTS = 300;
  private static final int[] REGULAR_ACCOUNTS;

  private static final int SUPER_ACCOUNT = 99999;

  static {
    Random random = new Random(42);
    REGULAR_ACCOUNTS = random.ints(50000, 90000)
        .limit(NR_OF_ACCOUNTS)
        .distinct()
        .toArray();

    if (REGULAR_ACCOUNTS.length != NR_OF_ACCOUNTS) {
      throw new IllegalStateException("The number of accounts does not match the expected value of " + NR_OF_ACCOUNTS);
    }
  }

  public static List<Integer> getRegularAccounts() {
    return Arrays.stream(REGULAR_ACCOUNTS).boxed().collect(toList());
  }

  public static int[] getRegularAccountsAsArray() {
    return Arrays.copyOf(REGULAR_ACCOUNTS, REGULAR_ACCOUNTS.length);
  }

  public static int getSuperAccount() {
    return SUPER_ACCOUNT;
  }
}
