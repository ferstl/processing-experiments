package com.github.ferstl.processing.cluster;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.agrona.ErrorHandler;
import org.agrona.concurrent.ShutdownSignalBarrier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.aeron.ChannelUriStringBuilder;
import io.aeron.CommonContext;
import io.aeron.archive.Archive;
import io.aeron.archive.ArchiveThreadingMode;
import io.aeron.archive.client.AeronArchive;
import io.aeron.cluster.ConsensusModule;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.cluster.service.ClusteredServiceContainer;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.MinMulticastFlowControlSupplier;
import io.aeron.driver.ThreadingMode;
import static com.github.ferstl.processing.cluster.PortUtil.calculatePort;

@Configuration
public class ClusterConfiguration {

  private static final int ARCHIVE_CONTROL_REQUEST_PORT_OFFSET = 1;
  private static final int ARCHIVE_CONTROL_RESPONSE_PORT_OFFSET = 2;
  private static final int CLIENT_FACING_PORT_OFFSET = 3;
  private static final int MEMBER_FACING_PORT_OFFSET = 4;
  private static final int LOG_PORT_OFFSET = 5;
  private static final int TRANSFER_PORT_OFFSET = 6;
  private static final int LOG_CONTROL_PORT_OFFSET = 7;

  @Value("${processing.nodeId}")
  private int nodeId;

  @Bean
  public String aeronDirectoryName() {
    return CommonContext.getAeronDirectoryName() + "-" + this.nodeId + "-driver";
  }

  @Bean
  public Path baseDir() {
    return Paths.get(System.getProperty("user.dir"), "node" + this.nodeId);
  }

  @Bean
  public ShutdownSignalBarrier shutdownSignalBarrier() {
    return new ShutdownSignalBarrier();
  }

  @Bean
  public List<String> hostNames() {
    return Arrays.asList("localhost", "localhost", "localhost");
  }

  @Bean
  public MediaDriver.Context mediaDriverContext() {
    ShutdownSignalBarrier shutdownSignalBarrier = shutdownSignalBarrier();

    return new MediaDriver.Context()
        .aeronDirectoryName(aeronDirectoryName())
        .threadingMode(ThreadingMode.SHARED)
        .termBufferSparseFile(true)
        .multicastFlowControlSupplier(new MinMulticastFlowControlSupplier())
        .terminationHook(shutdownSignalBarrier::signal)
        .errorHandler(errorHandler("Media Driver"));
  }

  @Bean
  public Archive.Context archiveContext() {
    return new Archive.Context()
        .aeronDirectoryName(aeronDirectoryName())
        .archiveDir(baseDir().resolve("archive").toFile())
        .controlChannel(udpChannel(this.nodeId, "localhost", ARCHIVE_CONTROL_REQUEST_PORT_OFFSET))
        .localControlChannel("aeron:ipc?term-length=64k")
        .recordingEventsEnabled(false)
        .threadingMode(ArchiveThreadingMode.SHARED);
  }

  @Bean
  public AeronArchive.Context aeronArchiveContext() {
    Archive.Context archiveContext = archiveContext();
    return new AeronArchive.Context()
        .controlRequestChannel(archiveContext.controlChannel())
        .controlRequestStreamId(archiveContext.controlStreamId())
        .controlResponseChannel(udpChannel(this.nodeId, "localhost", ARCHIVE_CONTROL_RESPONSE_PORT_OFFSET))
        .aeronDirectoryName(aeronDirectoryName());
  }

  @Bean
  public ConsensusModule.Context consensusModuleContext() {
    return new ConsensusModule.Context()
        .errorHandler(errorHandler("Consensus Module"))
        .clusterMemberId(this.nodeId)
        .clusterMembers(clusterMembers(hostNames()))
        .aeronDirectoryName(aeronDirectoryName()) // directory of the media driver
        .clusterDir(baseDir().resolve("consensus-module").toFile())
        .ingressChannel("aeron:udp?term-length=64k")
        .logChannel(logControlChannel(this.nodeId, hostNames().get(this.nodeId), LOG_CONTROL_PORT_OFFSET))
        .archiveContext(aeronArchiveContext().clone());
  }

  @Bean
  public ClusteredServiceContainer.Context clusteredServiceContainerContext(ClusteredService service) {
    return new ClusteredServiceContainer.Context()
        .aeronDirectoryName(aeronDirectoryName())
        .archiveContext(aeronArchiveContext().clone())
        .clusterDir(baseDir().resolve("service").toFile())
        .clusteredService(service)
        .errorHandler(errorHandler("Clustered Service"));
  }

  @Bean
  public ClusterNode clusterNode(ClusteredServiceContainer.Context serviceContainerContext) {
    return new ClusterNode(
        this.nodeId,
        mediaDriverContext(),
        archiveContext(),
        consensusModuleContext(),
        serviceContainerContext,
        shutdownSignalBarrier()
    );
  }

  @Bean
  @ConditionalOnProperty(name = "cluster.node.type", havingValue = "accounting")
  public ClusteredService clusteredAccountingService() {
    return new ClusteredAccountingService(this.nodeId);
  }

  private static ErrorHandler errorHandler(String context) {
    return
        (Throwable throwable) ->
        {
          System.err.println(context);
          throwable.printStackTrace(System.err);
        };
  }

  private static String udpChannel(int nodeId, String hostname, int portOffset) {
    int port = calculatePort(nodeId, portOffset);
    return new ChannelUriStringBuilder()
        .media("udp")
        .termLength(64 * 1024)
        .endpoint(hostname + ":" + port)
        .build();
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
