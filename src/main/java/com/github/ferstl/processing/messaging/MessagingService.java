package com.github.ferstl.processing.messaging;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.concurrent.BackoffIdleStrategy;
import com.github.ferstl.processing.accounting.AccountingResult;
import com.github.ferstl.processing.accounting.AccountingStatus;
import com.github.ferstl.processing.event.codec.CommunicationEvent;
import com.github.ferstl.processing.event.codec.InboundMessage;
import com.github.ferstl.processing.event.codec.Reservation;
import com.github.ferstl.processing.event.codec.codec.CommunicationEventCodec;
import com.github.ferstl.processing.event.codec.codec.ReservationCodec;
import com.github.ferstl.processing.message.MessageService;
import com.github.ferstl.processing.message.Payment;
import io.aeron.cluster.service.Cluster;

public class MessagingService {


  private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
  private final Cluster cluster;
  private final int memberId;

  private final CommunicationEventCodec communicationEventCodec;
  private final ExpandableArrayBuffer sendBuffer;
  private final Map<String, AccountingResult> pendingResults;
  private final Map<UUID, Integer> senderIds;
  private final BackoffIdleStrategy idleStrategy;
  private final MessageService messageService;
  private final ReservationCodec reservationCodec;

  public MessagingService(Cluster cluster, int memberId) {
    this.cluster = cluster;
    this.memberId = memberId;
    this.communicationEventCodec = new CommunicationEventCodec();
    this.reservationCodec = new ReservationCodec();
    this.sendBuffer = new ExpandableArrayBuffer();
    this.pendingResults = new LinkedHashMap<>();
    this.idleStrategy = new BackoffIdleStrategy();
    this.messageService = new MessageService();
    this.senderIds = new HashMap<>();
  }

  public void handleAccountingResult(int memberId, AccountingResult accountingResult, boolean force) {
    Integer senderId = this.senderIds.get(accountingResult.getCorrelationId());
    if (senderId != null) {
      if (senderId % 3 == this.memberId) {
        // If message arrived from same member: handle it and notify the cluster
        System.out.println(String.format("<- Sending %s: %s", accountingResult.getCorrelationId(), accountingResult.getAccountingStatus()));
        CommunicationEvent communicationEvent = new CommunicationEvent(accountingResult.getCorrelationId(), accountingResult.getAccountingStatus());
        int encodedLength = this.communicationEventCodec.encode(communicationEvent, this.sendBuffer, 0);
        while (this.cluster.offer(this.sendBuffer, 0, encodedLength) < 0) {
          System.out.println("Cluster not available");
          this.idleStrategy.idle();
        }
      } else {
        // otherwise just buffer it
        System.out.println(String.format("-> Buffering %s: %s", accountingResult.getCorrelationId(), accountingResult.getAccountingStatus()));
        this.pendingResults.put(createKey(accountingResult), accountingResult);
      }
    } else {
      System.out.println("Unknown sender " + senderId);
    }
  }

  public void communicated(CommunicationEvent communicationEvent) {
    this.pendingResults.remove(createKey(communicationEvent));
    Integer senderId = this.senderIds.get(communicationEvent.getCorrelationId());
    if (senderId != null) {
      if (senderId % 3 != this.memberId) {
        System.out.println("<- Noticed sent message " + communicationEvent.getCorrelationId());
      }
    }
  }

  public void takeOver() {
    // TODO What happens if another node takes over during this call?
    System.out.println();
    System.out.println("********************************************");
    System.out.println("Taking over! Start handling pending messages");
    System.out.println("********************************************");
    System.out.println();

    Iterator<AccountingResult> iterator = this.pendingResults.values().iterator();
    while (iterator.hasNext()) {
      AccountingResult result = iterator.next();
      handleAccountingResult(this.memberId, result, true);
      iterator.remove();
    }

    System.out.println("********************************************");
    System.out.println();
  }


  private static String createKey(AccountingResult accountingResult) {
    return createKey(accountingResult.getCorrelationId(), accountingResult.getAccountingStatus());
  }

  private static String createKey(CommunicationEvent communicationEvent) {
    return createKey(communicationEvent.getCorrelationId(), communicationEvent.getAccountingStatus());
  }

  private static String createKey(UUID correlationId, AccountingStatus accountingStatus) {
    return correlationId.toString() + ": " + accountingStatus.name();
  }

  public void handleInboundMessage(InboundMessage inboundMessage) {
    int senderId = inboundMessage.getSenderId();
    Payment payment = this.messageService.readPayment(inboundMessage.getData());

    this.senderIds.put(inboundMessage.getCorrelationId(), inboundMessage.getSenderId());

    // Simulate arrival at a specific node
    if (senderId % 3 == this.memberId) {
      System.out.println("-> Received Message " + inboundMessage.getCorrelationId());
      Reservation reservation = new Reservation(
          inboundMessage.getCorrelationId(),
          Integer.parseInt(payment.getDebtorAccount()),
          Integer.parseInt(payment.getCreditorAccount()),
          payment.getAmount().multiply(ONE_HUNDRED).longValue()
      );

      int encodedLength = this.reservationCodec.encode(reservation, this.sendBuffer, 0);
      while (this.cluster.offer(this.sendBuffer, 0, encodedLength) < 0) {
        System.out.println("Cluster not available");
        this.idleStrategy.idle();
      }
    }
  }
}
