package com.github.ferstl.processing;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import com.github.ferstl.processing.event.EventType;
import com.github.ferstl.processing.event.ReservationEvent;
import com.github.ferstl.processing.event.SettlementResponse;
import io.aeron.cluster.client.AeronCluster;
import io.aeron.cluster.client.EgressListener;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import io.aeron.logbuffer.Header;
import static io.aeron.samples.cluster.BasicAuctionClusteredServiceNode.CLIENT_FACING_PORT_OFFSET;

public class AccountingClient implements EgressListener {

  private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

  private final IdleStrategy idleStrategy = new BackoffIdleStrategy();
  private final MutableDirectBuffer sendBuffer = new ExpandableDirectByteBuffer();

  public static void main(String[] args) throws InterruptedException {
    int customerId = Integer.parseInt(System.getProperty("processing.clientId"));
    int egressPort = 19000 + customerId;
    AccountingClient accountingClient = new AccountingClient();
    String ingressEndpoints = ingressEndpoints(Arrays.asList("localhost", "localhost", "localhost"));

    try (
        MediaDriver mediaDriver = MediaDriver.launchEmbedded(new MediaDriver.Context()
            .threadingMode(ThreadingMode.SHARED)
            .dirDeleteOnStart(true)
            .dirDeleteOnShutdown(true));
        AeronCluster aeronCluster = AeronCluster.connect(
            new AeronCluster.Context()
                .egressListener(accountingClient)
                .egressChannel("aeron:udp?endpoint=localhost:" + egressPort)
                .aeronDirectoryName(mediaDriver.aeronDirectoryName())
                .ingressChannel("aeron:udp")
                .ingressEndpoints(ingressEndpoints))) {
      for (int i = 0; i < 100; i++) {
        accountingClient.reserve(aeronCluster);
      }

      while (true) {
        accountingClient.idleStrategy.idle(aeronCluster.pollEgress());
      }
    }
  }

  @Override
  public void onMessage(long clusterSessionId, long timestamp, DirectBuffer buffer, int offset, int length, Header header) {
    SettlementResponse settlementResponse = SettlementResponse.deserialize(buffer, offset);
    System.out.println("<- Reservation " + settlementResponse.getCorrelationId() + " was " + (settlementResponse.isSettled() ? "successful" : "not successful") + " " + Thread.currentThread().getName());
  }

  private void reserve(AeronCluster cluster) {

    ReservationEvent reservationEvent = new ReservationEvent(UUID.randomUUID(), AccountUtil.randomAccount(), AccountUtil.randomAccount(), AccountUtil.randomAmount().multiply(ONE_HUNDRED).longValue());
    int eventTypeOffset = this.sendBuffer.putStringAscii(0, EventType.forEvent(reservationEvent).name());
    reservationEvent.serialize(this.sendBuffer, eventTypeOffset);

    System.out.println(String.format("-> Sending message %s: %06d --- %d --> %06d",
        reservationEvent.getCorrelationId(),
        reservationEvent.getDebtorAccount(),
        reservationEvent.getAmount(),
        reservationEvent.getCreditorAccount()) + " " + Thread.currentThread().getName());

    while (cluster.offer(this.sendBuffer, 0, reservationEvent.getSerializedLength() + eventTypeOffset) < 0) {
      this.idleStrategy.idle(cluster.pollEgress());
    }

    this.idleStrategy.idle(cluster.pollEgress());
  }


  private static String ingressEndpoints(List<String> hostnames) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < hostnames.size(); i++) {
      sb.append(i).append('=');
      sb.append(hostnames.get(i)).append(':').append(ClusterNode.calculatePort(i, CLIENT_FACING_PORT_OFFSET));
      sb.append(',');
    }

    sb.setLength(sb.length() - 1);

    return sb.toString();
  }
}
