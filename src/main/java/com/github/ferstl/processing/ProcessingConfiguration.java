package com.github.ferstl.processing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import com.github.ferstl.processing.journal.JournalProcessor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;

@Configuration
@PropertySource("classpath:processing.properties")
public class ProcessingConfiguration {

  private static final LocalDateTypeAdapter LOCAL_DATE_TYPE_ADAPTER = new LocalDateTypeAdapter();

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
      return ChronicleQueueBuilder
          .indexed(new File(inboundJournal))
          .synchronous(false)
          .build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Bean
  public SubmissionProcessor submissionProcessor() {
    return new SubmissionProcessor(inboundJournal());
  }

  @Bean
  public Gson gson() {
    return new GsonBuilder().registerTypeAdapter(LocalDate.class, LOCAL_DATE_TYPE_ADAPTER).create();
  }

  static class LocalDateTypeAdapter extends TypeAdapter<LocalDate> {

    @Override
    public void write(JsonWriter out, LocalDate value) throws IOException {
      out.value(value.toString());
    }

    @Override
    public LocalDate read(JsonReader in) throws IOException {
      return LocalDate.parse(in.nextString());
    }

  }

}
