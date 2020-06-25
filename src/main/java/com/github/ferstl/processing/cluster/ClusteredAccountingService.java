package com.github.ferstl.processing.cluster;

import org.agrona.DirectBuffer;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.IdleStrategy;
import com.github.ferstl.processing.accounting.AccountingResult;
import com.github.ferstl.processing.accounting.AccountingService;
import com.github.ferstl.processing.event.codec.codec.AccountingResultCodec;
import com.github.ferstl.processing.messaging.MessagingService;
import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.Cluster.Role;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.logbuffer.Header;

public class ClusteredAccountingService implements ClusteredService {

  private final AccountingResultCodec accountingResultCodec;

  private final int memberId;

  private EventDispatcher eventDispatcher;

  private Cluster cluster;
  private IdleStrategy idleStrategy;


  private final MutableDirectBuffer responseBuffer = new ExpandableDirectByteBuffer(4);
  private MessagingService messagingService;

  public ClusteredAccountingService(int memberId) {
    this.memberId = memberId;
    this.accountingResultCodec = new AccountingResultCodec();
  }

  @Override
  public void onStart(Cluster cluster, Image snapshotImage) {
    this.cluster = cluster;
    this.messagingService = new MessagingService(cluster, this.memberId);
    this.eventDispatcher = new EventDispatcher(this.memberId, new AccountingService(), this.messagingService);
    this.idleStrategy = cluster.idleStrategy();

    if (snapshotImage != null) {
      loadSnapshot(cluster, snapshotImage);
    }
  }

  @Override
  public void onSessionOpen(ClientSession session, long timestamp) {
    System.out.println("Session opened: " + session.id());
  }

  @Override
  public void onSessionClose(ClientSession session, long timestamp, CloseReason closeReason) {
    System.out.println("Session closed: " + session.id() + ", Reason " + closeReason);
  }

  @Override
  public void onSessionMessage(ClientSession session, long timestamp, DirectBuffer buffer, int offset, int length, Header header) {
    AccountingResult result = this.eventDispatcher.dispatch(buffer, offset);

    if (result != null) {
      this.messagingService.handleAccountingResult(this.cluster.memberId(), result);
    }

    // session == null: recovery
    if (session != null && result != null) {
      int encodedLength = this.accountingResultCodec.encode(result, this.responseBuffer, 0);
      System.out.println("<- Result for " + result.getCorrelationId() + " is " + result.getAccountingStatus());
      while (session.offer(this.responseBuffer, 0, encodedLength) < 0) {
        this.idleStrategy.idle();
      }
    }
  }

  @Override
  public void onTimerEvent(long correlationId, long timestamp) {

  }

  @Override
  public void onTakeSnapshot(ExclusivePublication snapshotPublication) {

  }

  @Override
  public void onRoleChange(Role newRole) {

  }

  @Override
  public void onTerminate(Cluster cluster) {

  }

  private void loadSnapshot(Cluster cluster, Image snapshotImage) {
    // TODO: 13.06.2020 Implement
  }
}
