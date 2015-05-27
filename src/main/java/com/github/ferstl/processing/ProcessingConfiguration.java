package com.github.ferstl.processing;

import java.io.File;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;

@Configuration
@PropertySource("classpath:processing.properties")
public class ProcessingConfiguration {

  @Autowired
  private Environment env;

  @Bean
  public Chronicle inboundJournal() throws IOException {
    String inboundJournal = this.env.getProperty("inbound.journal");
    return ChronicleQueueBuilder.indexed(new File(inboundJournal)).synchronous(false).messageCapacity(10000).build();
  }
}
