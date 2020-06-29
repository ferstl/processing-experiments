package com.github.ferstl.processing.cluster;

import org.agrona.DirectBuffer;
import io.aeron.ExclusivePublication;
import io.aeron.Image;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.cluster.service.ClientSession;
import io.aeron.cluster.service.Cluster;
import io.aeron.cluster.service.Cluster.Role;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.logbuffer.Header;

public class ClusteredMessagingService implements ClusteredService {

  private final int memberId;
  private Cluster cluster;

  public ClusteredMessagingService(int memberId) {
    this.memberId = memberId;
  }

  @Override
  public void onStart(Cluster cluster, Image snapshotImage) {
    this.cluster = cluster;
  }

  @Override
  public void onSessionOpen(ClientSession session, long timestamp) {

  }

  @Override
  public void onSessionClose(ClientSession session, long timestamp, CloseReason closeReason) {

  }

  @Override
  public void onSessionMessage(ClientSession session, long timestamp, DirectBuffer buffer, int offset, int length, Header header) {

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
}
