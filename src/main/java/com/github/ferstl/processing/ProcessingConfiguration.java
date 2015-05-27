package com.github.ferstl.processing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import com.github.ferstl.processing.journal.JournalProcessor;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;

@Configuration
@PropertySource("classpath:processing.properties")
public class ProcessingConfiguration {

  @Autowired
  private Environment env;

  @Bean
  public JournalProcessor inboundJournalProcessor() {
    String inboundJournal = this.env.getProperty("inbound.journal");
    return new JournalProcessor(Paths.get(inboundJournal), inboundQueue().get());
  }

  // Cannot autowire collections
  @Bean
  public Supplier<BlockingQueue<String>> inboundQueue() {
    BlockingQueue<String> queue = new ArrayBlockingQueue<>(1000);
    return () -> queue;
  }

  // TODO: Find a better place to hold the chronicles
  @Bean
  public Chronicle inboundJournal() {
    String inboundJournal = this.env.getProperty("inbound.journal");
    try {
      return ChronicleQueueBuilder.indexed(new File(inboundJournal)).synchronous(false).messageCapacity(10000).build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Bean
  public SubmissionProcessor submissionProcessor() {
    return new SubmissionProcessor(inboundJournal());
  }
}
