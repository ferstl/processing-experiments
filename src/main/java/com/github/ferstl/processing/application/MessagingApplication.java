package com.github.ferstl.processing.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.jetty.ConfigurableJettyWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import com.github.ferstl.processing.cluster.ClusterConfiguration;
import com.github.ferstl.processing.cluster.ClusteredMessagingService;
import io.aeron.cluster.service.ClusteredService;
import io.aeron.cluster.service.ClusteredServiceContainer;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.MediaDriver.Context;

@SpringBootApplication
@Import(ClusterConfiguration.class)
public class MessagingApplication {

  @Value("${processing.basePort}")
  private int processingBasePort;

  @Value("${processing.nodeId}")
  private int processingNodeId;

  public static void main(String[] args) {
    ConfigurableApplicationContext ctx = SpringApplication.run(MessagingApplication.class);
    MediaDriver.Context mediaDriverContext = ctx.getBean(Context.class);
    ClusteredServiceContainer.Context serviceContainerContext = ctx.getBean(ClusteredServiceContainer.Context.class);

    MediaDriver mediaDriver = MediaDriver.launch(mediaDriverContext);
    ClusteredServiceContainer clusteredServiceContainer = ClusteredServiceContainer.launch(serviceContainerContext);
  }

  @Bean
  public ClusteredService messagingService() {
    return new ClusteredMessagingService(this.processingNodeId);
  }

  @Bean
  public WebServerFactoryCustomizer<ConfigurableJettyWebServerFactory> webServerFactoryCustomizer() {
    return webServerFactory -> webServerFactory.setPort(this.processingBasePort + 80 + this.processingNodeId);
  }
}
