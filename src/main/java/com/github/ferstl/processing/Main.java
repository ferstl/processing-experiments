package com.github.ferstl.processing;

import java.math.BigDecimal;
import java.time.Duration;
import com.github.ferstl.processing.model.Payment;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

public class Main {

  private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

  public static void main(String[] args) throws Exception {
    // Specify the size of the ring buffer, must be power of 2.
    int bufferSize = 1024;

    // Construct the Disruptor
    Disruptor<Message> disruptor = new Disruptor<>(Message::new, bufferSize, DaemonThreadFactory.INSTANCE);

    JournalingService journalingService = new JournalingService();
    MessageService messageService = new MessageService();
    AccountingService accountingService = new AccountingService();
    // Connect the handlers
    disruptor
        .handleEventsWith(
            (message, sequence, endOfBatch) -> journalingService.writeMessage(message),
            (message, sequence, endOfBatch) -> message.setParsedMessage(messageService.readPayment(message.getData())))
        .then((message, sequence, endOfBatch) -> {
          Payment payment = message.getPayment();
          accountingService.transfer(Integer.parseInt(payment.getDebtorAccount()), Integer.parseInt(payment.getCreditorAccount()), payment.getAmount().multiply(ONE_HUNDRED).longValue());
        });

    // Start the Disruptor, starts all threads running
    // Get the ring buffer from the Disruptor to be used for publishing.
    RingBuffer<Message> ringBuffer = disruptor.start();

    MessageProducer messageProducer = new MessageProducer(messageService);

    long startTime = System.currentTimeMillis();
    for (long l = 0; l < 10000; l++) {
      ringBuffer.publishEvent((event, sequence, producer) -> {
        producer.producePayment(event);
      }, messageProducer);
    }

    disruptor.shutdown();
    Duration duration = Duration.ofMillis(System.currentTimeMillis() - startTime);
    journalingService.close();
    System.out.println("Processing took " + duration);

    long totalBalance = accountingService.getTotalBalance();
    if (totalBalance != 0) {
      throw new IllegalStateException("Invalid balance at the end of processing " + totalBalance);
    }

  }
}
