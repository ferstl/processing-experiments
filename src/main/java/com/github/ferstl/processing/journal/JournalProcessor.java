package com.github.ferstl.processing.journal;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.github.ferstl.processing.model.Message;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;

public class JournalProcessor implements Closeable {

  private final Path chroniclePath;
  private final BlockingQueue<String> messageQueue;
  private final ExecutorService journalingExecutor;
  private volatile Chronicle chronicle;
  private volatile ExcerptAppender appender;
  private volatile Future<?> journalingFuture;
  private volatile boolean isStarted;


  public JournalProcessor(Path chroniclePath, BlockingQueue<String> messageQueue) {
    this.chroniclePath = chroniclePath;
    this.messageQueue = messageQueue;
    this.journalingExecutor = Executors.newSingleThreadExecutor();
  }

  public void start() {
    try {
      this.chronicle = ChronicleQueueBuilder.indexed(this.chroniclePath.toFile()).synchronous(false).messageCapacity(10000).build();
      this.appender = this.chronicle.createAppender();
      this.isStarted = true;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    this.journalingFuture = this.journalingExecutor.submit(new JournalingTask(this.messageQueue, this.appender));
  }

  public void stop() {
    if (!this.isStarted) {
      return;
    }

    try {
      this.journalingFuture.cancel(true);
    } finally {
      closeJournal();
      this.isStarted = false;
    }
  }

  @Override
  public void close() {
    stop();
  }

  private void closeJournal() {
    this.appender.close();
    try {
      this.chronicle.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static class JournalingTask implements Runnable {

    private final BlockingQueue<String> messageQueue;
    private final ExcerptAppender appender;
    private final Message message;

    public JournalingTask(BlockingQueue<String> messageQueue, ExcerptAppender appender) {
      this.messageQueue = messageQueue;
      this.appender = appender;
      this.message = new Message();
    }


    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          journal();
        } catch (InterruptedException e) {
          // Stop
          Thread.currentThread().interrupt();
          return;
        }
      }
    }


    private void journal() throws InterruptedException {
      String data = this.messageQueue.take();
      this.appender.startExcerpt(data.length() + 100);
      this.message.newCorrelationId();
      this.message.setData(data);
      this.message.writeMarshallable(this.appender);
      this.appender.finish();
    }

  }

}
