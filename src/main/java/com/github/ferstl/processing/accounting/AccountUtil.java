package com.github.ferstl.processing.accounting;

import java.math.BigDecimal;
import java.util.Random;
import org.jetbrains.annotations.NotNull;
import static com.github.ferstl.processing.accounting.AccountMasterdata.getRegularAccountsAsArray;

public class AccountUtil {

  private static final int NR_OF_AMOUNTS = 10000;
  private static final int[] accounts;
  private static final String[] amounts;
  private static final Random random;

  static {
    random = new Random(42);
    accounts = getRegularAccountsAsArray();

    int[] integers = random.ints(100, 10000)
        .limit(NR_OF_AMOUNTS)
        .toArray();

    int[] fractions = random.ints(0, 100)
        .limit(NR_OF_AMOUNTS)
        .toArray();

    amounts = new String[NR_OF_AMOUNTS];
    for (int i = 0; i < integers.length; i++) {
      amounts[i] = integers[i] + "." + fractions[i];
    }
  }

  @NotNull
  public static BigDecimal randomAmount() {
    return new BigDecimal(amounts[random.nextInt(amounts.length)]);
  }

  public static int randomAccount() {
    return accounts[random.nextInt(accounts.length)];
  }

}
