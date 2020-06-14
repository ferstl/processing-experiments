package com.github.ferstl.processing;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.agrona.ErrorHandler;
import org.agrona.concurrent.ShutdownSignalBarrier;
import io.aeron.ChannelUriStringBuilder;
import io.aeron.CommonContext;
import io.aeron.archive.Archive;
import io.aeron.archive.ArchiveThreadingMode;
import io.aeron.archive.client.AeronArchive;
import io.aeron.cluster.ClusteredMediaDriver;
import io.aeron.cluster.ConsensusModule;
import io.aeron.cluster.service.ClusteredServiceContainer;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.MinMulticastFlowControlSupplier;
import io.aeron.driver.ThreadingMode;

public class ClusterNode {

  private static final int PORT_BASE = 9000;
  private static final int PORTS_PER_NODE = 100;
  private static final int ARCHIVE_CONTROL_REQUEST_PORT_OFFSET = 1;
  private static final int ARCHIVE_CONTROL_RESPONSE_PORT_OFFSET = 2;
  private static final int CLIENT_FACING_PORT_OFFSET = 3;
  private static final int MEMBER_FACING_PORT_OFFSET = 4;
  private static final int LOG_PORT_OFFSET = 5;
  private static final int TRANSFER_PORT_OFFSET = 6;
  private static final int LOG_CONTROL_PORT_OFFSET = 7;

  public static void main(String[] args) {
    int nodeId = Integer.parseInt(System.getProperty("processing.nodeId"));
    List<String> hostnames = Arrays.asList("localhost", "localhost", "localhost");
    String hostname = hostnames.get(nodeId);

    Path baseDir = Paths.get(System.getProperty("user.dir"), "node" + nodeId);
    String aeronDirName = CommonContext.getAeronDirectoryName() + "-" + nodeId + "-driver";

    ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();

    MediaDriver.Context mediaDriverContext = new MediaDriver.Context()
        .aeronDirectoryName(aeronDirName)
        .threadingMode(ThreadingMode.SHARED)
        .termBufferSparseFile(true)
        .multicastFlowControlSupplier(new MinMulticastFlowControlSupplier())
        .terminationHook(barrier::signal)
        .errorHandler(errorHandler("Media Driver"));

    Archive.Context archiveContext = new Archive.Context()
        .aeronDirectoryName(aeronDirName)
        .archiveDir(baseDir.resolve("archive").toFile())
        .controlChannel(udpChannel(nodeId, "localhost", ARCHIVE_CONTROL_REQUEST_PORT_OFFSET))
        .localControlChannel("aeron:ipc?term-length=64k")
        .recordingEventsEnabled(false)
        .threadingMode(ArchiveThreadingMode.SHARED);

    AeronArchive.Context aeronArchiveContext = new AeronArchive.Context()
        .controlRequestChannel(archiveContext.controlChannel())
        .controlRequestStreamId(archiveContext.controlStreamId())
        .controlResponseChannel(udpChannel(nodeId, "localhost", ARCHIVE_CONTROL_RESPONSE_PORT_OFFSET))
        .aeronDirectoryName(aeronDirName);

    ConsensusModule.Context consensusModuleContext = new ConsensusModule.Context()
        .errorHandler(errorHandler("Consensus Module"))
        .clusterMemberId(nodeId)
        .clusterMembers(clusterMembers(hostnames))
        .aeronDirectoryName(aeronDirName) // directory of the media driver
        .clusterDir(baseDir.resolve("consensus-module").toFile())
        .ingressChannel("aeron:udp?term-length=64k")
        .logChannel(logControlChannel(nodeId, hostname, LOG_CONTROL_PORT_OFFSET))
        .archiveContext(aeronArchiveContext.clone());

    ClusteredServiceContainer.Context clusteredServiceContext =
        new ClusteredServiceContainer.Context()
            .aeronDirectoryName(aeronDirName)
            .archiveContext(aeronArchiveContext.clone())
            .clusterDir(baseDir.resolve("service").toFile())
            .clusteredService(new ClusteredAccountingService())
            .errorHandler(errorHandler("Clustered Service"));

    try (ClusteredMediaDriver clusteredMediaDriver = ClusteredMediaDriver.launch(mediaDriverContext, archiveContext, consensusModuleContext);
        ClusteredServiceContainer container = ClusteredServiceContainer.launch(clusteredServiceContext)) {

      System.out.println("[" + nodeId + "] Started Cluster Node on " + hostname + "...");
      barrier.await();
      System.out.println("[" + nodeId + "] Exiting");
    }
  }


  public static int calculatePort(int nodeId, int offset) {
    return PORT_BASE + (nodeId * PORTS_PER_NODE) + offset;
  }

  private static String udpChannel(int nodeId, String hostname, int portOffset) {
    int port = calculatePort(nodeId, portOffset);
    return new ChannelUriStringBuilder()
        .media("udp")
        .termLength(64 * 1024)
        .endpoint(hostname + ":" + port)
        .build();
  }

  private static ErrorHandler errorHandler(String context) {
    return
        (Throwable throwable) ->
        {
          System.err.println(context);
          throwable.printStackTrace(System.err);
        };
  }

  private static String clusterMembers(List<String> hostnames) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < hostnames.size(); i++) {
      sb.append(i);
      sb.append(',').append(hostnames.get(i)).append(':').append(calculatePort(i, CLIENT_FACING_PORT_OFFSET));
      sb.append(',').append(hostnames.get(i)).append(':').append(calculatePort(i, MEMBER_FACING_PORT_OFFSET));
      sb.append(',').append(hostnames.get(i)).append(':').append(calculatePort(i, LOG_PORT_OFFSET));
      sb.append(',').append(hostnames.get(i)).append(':').append(calculatePort(i, TRANSFER_PORT_OFFSET));
      sb.append(',').append(hostnames.get(i)).append(':')
          .append(calculatePort(i, ARCHIVE_CONTROL_REQUEST_PORT_OFFSET));
      sb.append('|');
    }

    return sb.toString();
  }

  private static String logControlChannel(int nodeId, String hostname, int portOffset) {
    int port = calculatePort(nodeId, portOffset);
    return new ChannelUriStringBuilder()
        .media("udp")
        .termLength(64 * 1024)
        .controlMode("manual")
        .controlEndpoint(hostname + ":" + port)
        .build();
  }
}
