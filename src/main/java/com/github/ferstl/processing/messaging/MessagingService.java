package com.github.ferstl.processing.messaging;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.agrona.ExpandableArrayBuffer;
import com.github.ferstl.processing.accounting.AccountingResult;
import com.github.ferstl.processing.accounting.AccountingStatus;
import com.github.ferstl.processing.event.codec.CommunicationEvent;
import com.github.ferstl.processing.event.codec.codec.CommunicationEventCodec;
import io.aeron.cluster.service.Cluster;

public class MessagingService {


  private final Cluster cluster;
  private final int memberId;

  private final CommunicationEventCodec communicationEventCodec;
  private final ExpandableArrayBuffer sendBuffer;
  private final Map<String, AccountingResult> pendingResults;

  public MessagingService(Cluster cluster, int memberId) {
    this.cluster = cluster;
    this.memberId = memberId;
    this.communicationEventCodec = new CommunicationEventCodec();
    this.sendBuffer = new ExpandableArrayBuffer();
    this.pendingResults = new LinkedHashMap<>();
  }

  public void handleAccountingResult(int memberId, AccountingResult accountingResult) {
    if (memberId == this.memberId) {
      // If message arrived from same member: handle it and notify the cluster
      System.out.println(String.format("Sending %s: %s", accountingResult.getCorrelationId(), accountingResult.getAccountingStatus()));
      CommunicationEvent communicationEvent = new CommunicationEvent(accountingResult.getCorrelationId(), accountingResult.getAccountingStatus());
      int encodedLength = this.communicationEventCodec.encode(communicationEvent, this.sendBuffer, 0);
      this.cluster.offer(this.sendBuffer, 0, encodedLength);
    } else {
      // otherwise just buffer it
      this.pendingResults.put(createKey(accountingResult), accountingResult);
    }
  }

  public void communicated(CommunicationEvent communicationEvent) {
    this.pendingResults.remove(createKey(communicationEvent));
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
      handleAccountingResult(this.memberId, result);
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
}
