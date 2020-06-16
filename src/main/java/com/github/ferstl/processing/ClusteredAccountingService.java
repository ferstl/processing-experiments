package com.github.ferstl.processing;

import org.agrona.DirectBuffer;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.IdleStrategy;
import com.github.ferstl.processing.event.SettlementEvent;
import com.github.ferstl.processing.event.SettlementResponse;
import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.Cluster.Role;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.logbuffer.Header;

public class ClusteredAccountingService implements ClusteredService {

  private Cluster cluster;
  private IdleStrategy idleStrategy;
  private AccountingService accountingService;

  private final MutableDirectBuffer responseBuffer = new ExpandableDirectByteBuffer(4);

  @Override
  public void onStart(Cluster cluster, Image snapshotImage) {
    this.accountingService = new AccountingService();
    this.cluster = cluster;
    this.idleStrategy = cluster.idleStrategy();

    if (snapshotImage != null) {
      loadSnapshot(cluster, snapshotImage);
    }
  }

  @Override
  public void onSessionOpen(ClientSession session, long timestamp) {

  }

  @Override
  public void onSessionClose(ClientSession session, long timestamp, CloseReason closeReason) {

  }

  @Override
  public void onSessionMessage(ClientSession session, long timestamp, DirectBuffer buffer, int offset, int length, Header header) {
    SettlementEvent settlementEvent = SettlementEvent.deserialize(buffer, offset);
    this.accountingService.reserve(
        settlementEvent.getCorrelationId(),
        settlementEvent.getDebtorAccount(),
        settlementEvent.getCreditorAccount(),
        settlementEvent.getAmount());

    System.out.println("Received " + settlementEvent.getCorrelationId());
    // session == null: recovery
    if (session != null) {
      SettlementResponse settlementResponse = new SettlementResponse(settlementEvent.getCorrelationId(), true);
      settlementResponse.serialize(this.responseBuffer, 0);

      while (session.offer(this.responseBuffer, 0, settlementResponse.getSerializedLength()) < 0) {
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
