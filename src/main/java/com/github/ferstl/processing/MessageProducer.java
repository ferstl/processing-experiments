package com.github.ferstl.processing;

import java.math.BigDecimal;
import java.time.Instant;
import com.github.ferstl.processing.Message.Metadata;
import com.github.ferstl.processing.model.Payment;
import static com.github.ferstl.processing.AccountUtil.randomAccount;
import static com.github.ferstl.processing.AccountUtil.randomAmount;
import static java.util.UUID.randomUUID;

public class MessageProducer {

  private final MessageService messageService;


  public MessageProducer(MessageService messageService) {
    this.messageService = messageService;
  }

  public void producePayment(Message message) {
    String debtorAccount = randomAccount();
    String creditorAccount = randomAccount();
    BigDecimal amount = randomAmount();

    byte[] paymentData = this.messageService.writePayment(new Payment(debtorAccount, creditorAccount, amount));
    message.setData(paymentData);
    message.setMetadata(new Metadata(Instant.now(), randomUUID()));
  }

  public Payment producePayment(byte[] data) {
    return this.messageService.readPayment(data);
  }
}
