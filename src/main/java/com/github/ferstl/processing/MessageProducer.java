package com.github.ferstl.processing;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;
import org.jetbrains.annotations.NotNull;
import com.github.ferstl.processing.Message.Metadata;
import com.github.ferstl.processing.model.Payment;
import static com.github.ferstl.processing.AccountMasterdata.getAccountsAsArray;
import static java.util.UUID.randomUUID;

public class MessageProducer {

  private static final int NR_OF_AMOUNTS = 10000;
  private final String[] accounts;
  private final String[] amounts;
  private final Random random;

  private final MessageService messageService;


  public MessageProducer(MessageService messageService) {
    this.messageService = messageService;
    this.random = new Random(42);
    this.accounts = getAccountsAsArray();

    int[] integers = this.random.ints(100, 10000)
        .limit(NR_OF_AMOUNTS)
        .toArray();

    int[] fractions = this.random.ints(0, 100)
        .limit(NR_OF_AMOUNTS)
        .toArray();

    this.amounts = new String[NR_OF_AMOUNTS];
    for (int i = 0; i < integers.length; i++) {
      this.amounts[i] = integers[i] + "." + fractions[i];
    }
  }

  public void producePayment(Message message) {
    String debtorAccount = getAccount();
    String creditorAccount = getAccount();
    BigDecimal amount = getAmount();

    byte[] paymentData = this.messageService.writePayment(new Payment(debtorAccount, creditorAccount, amount));
    message.setData(paymentData);
    message.setMetadata(new Metadata(Instant.now(), randomUUID()));
  }

  @NotNull
  private BigDecimal getAmount() {
    return new BigDecimal(this.amounts[this.random.nextInt(this.amounts.length)]);
  }

  private String getAccount() {
    return this.accounts[this.random.nextInt(this.accounts.length)];
  }
}
