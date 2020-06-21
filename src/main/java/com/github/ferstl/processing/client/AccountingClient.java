package com.github.ferstl.processing.client;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import com.github.ferstl.processing.accounting.AccountUtil;
import com.github.ferstl.processing.accounting.AccountingResult;
import com.github.ferstl.processing.cluster.ClusterNode;
import com.github.ferstl.processing.event.codec.Reservation;
import com.github.ferstl.processing.event.codec.codec.AccountingResultCodec;
import com.github.ferstl.processing.event.codec.codec.ReservationCodec;
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

  private final ReservationCodec reservationCodec = new ReservationCodec();

  private final AccountingResultCodec accountingResultCodec = new AccountingResultCodec();

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
    AccountingResult accountingResult = this.accountingResultCodec.decode(buffer, offset);
    System.out.println("<- Result for " + accountingResult.getCorrelationId() + " was " + accountingResult.getAccountingStatus() + " " + Thread.currentThread().getName());
  }

  private void reserve(AeronCluster cluster) {
    Reservation reservation = new Reservation(UUID.randomUUID(), AccountUtil.randomAccount(), AccountUtil.randomAccount(), AccountUtil.randomAmount().multiply(ONE_HUNDRED).longValue());
    int encodedLength = this.reservationCodec.encode(reservation, this.sendBuffer, 0);

    System.out.println(String.format("-> Sending reservation %s: %06d --- %d --> %06d",
        reservation.getCorrelationId(),
        reservation.getDebtorAccount(),
        reservation.getAmount(),
        reservation.getCreditorAccount()) + " " + Thread.currentThread().getName());

    while (cluster.offer(this.sendBuffer, 0, encodedLength) < 0) {
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
