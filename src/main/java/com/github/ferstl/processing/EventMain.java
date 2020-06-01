package com.github.ferstl.processing;

import java.util.UUID;
import com.github.ferstl.processing.event.MessageInEvent;
import com.github.ferstl.processing.event.MessageOutEvent;
import com.github.ferstl.processing.event.PaymentEvent;
import com.github.ferstl.processing.model.Payment;
import static com.github.ferstl.processing.AccountUtil.randomAccount;
import static com.github.ferstl.processing.AccountUtil.randomAmount;
import static com.github.ferstl.processing.MessageTypeInbound.PAYMENT;
import static com.github.ferstl.processing.MessageTypeOutbound.CREDITOR_FINAL;
import static com.github.ferstl.processing.MessageTypeOutbound.DEBTOR_FINAL;
import static java.util.UUID.randomUUID;

public class EventMain {

  public static void main(String[] args) {
    MessageService messageService = new MessageService();
    MessageProducer messageProducer = new MessageProducer(messageService);
    EventService eventService = new EventService();
    AccountingService accountingService = new AccountingService();

    for (int i = 0; i < 10_000; i++) {
      // Outside world
      byte[] data = messageService.writePayment(new Payment(randomAccount(), randomAccount(), randomAmount()));


      UUID uuid = randomUUID();
      eventService.publishEvent(new MessageInEvent(uuid, PAYMENT, data));

      Payment payment = messageProducer.producePayment(data);
      accountingService.transfer(payment.getDebtorAccount(), payment.getCreditorAccount(), payment.getAmount());
      eventService.publishEvent(new PaymentEvent(uuid, payment.getDebtorAccount(), payment.getCreditorAccount(), payment.getAmount()));

      eventService.publishEvent(new MessageOutEvent(uuid, CREDITOR_FINAL, data));
      eventService.publishEvent(new MessageOutEvent(uuid, DEBTOR_FINAL, new byte[0]));
    }
  }
}