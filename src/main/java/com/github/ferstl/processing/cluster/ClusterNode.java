package com.github.ferstl.processing.cluster;

import org.agrona.concurrent.ShutdownSignalBarrier;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import io.aeron.archive.Archive;
import io.aeron.cluster.ClusteredMediaDriver;
import io.aeron.cluster.ConsensusModule;
import io.aeron.cluster.service.ClusteredServiceContainer;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.MediaDriver.Context;

public class ClusterNode {

  private final int nodeId;
  private final MediaDriver.Context mediaDriverContext;
  private final Archive.Context archiveContext;
  private final ConsensusModule.Context consensusModuleContext;
  private final ClusteredServiceContainer.Context clusteredServiceContainerContext;
  private final ShutdownSignalBarrier barrier;

  public ClusterNode(int nodeId, Context mediaDriverContext, Archive.Context archiveContext, ConsensusModule.Context consensusModuleContext, ClusteredServiceContainer.Context clusteredServiceContainerContext, ShutdownSignalBarrier barrier) {
    this.nodeId = nodeId;
    this.mediaDriverContext = mediaDriverContext;
    this.archiveContext = archiveContext;
    this.consensusModuleContext = consensusModuleContext;
    this.clusteredServiceContainerContext = clusteredServiceContainerContext;
    this.barrier = barrier;
  }

  public static void main(String[] args) {
    try (ConfigurableApplicationContext ctx = SpringApplication.run(ClusterConfiguration.class)) {
      ClusterNode clusterNode = ctx.getBean(ClusterNode.class);
      clusterNode.launch();
    }
  }

  public void launch() {
    try (ClusteredMediaDriver clusteredMediaDriver = ClusteredMediaDriver.launch(this.mediaDriverContext, this.archiveContext, this.consensusModuleContext);
        ClusteredServiceContainer container = ClusteredServiceContainer.launch(this.clusteredServiceContainerContext)) {

      System.out.println("[" + this.nodeId + "] Started Cluster Node");
      this.barrier.await();
      System.out.println("[" + this.nodeId + "] Exiting");
    }
  }
}
