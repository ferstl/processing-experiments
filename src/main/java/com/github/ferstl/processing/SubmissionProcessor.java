package com.github.ferstl.processing;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.github.ferstl.processing.model.Message;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptTailer;

public class SubmissionProcessor implements Closeable {

  private final ExecutorService submissionExecutor;
  private final Chronicle inboundJournal;
  private volatile ExcerptTailer tailer;
  private volatile boolean isStarted;
  private Future<?> submissionFuture;


  public SubmissionProcessor(Chronicle inboundJournal) {
    this.inboundJournal = inboundJournal;
    this.submissionExecutor = Executors.newSingleThreadExecutor();
  }

  public void start() {
    try {
      this.tailer = this.inboundJournal.createTailer();
      this.submissionFuture = this.submissionExecutor.submit(new SubmissionTask(this.tailer));
      this.isStarted = true;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    if (!this.isStarted) {
      return;
    }

    try {
      this.submissionFuture.cancel(true);
    } finally {
      closeInboundJournal();
      this.isStarted = false;
    }
  }

  @Override
  public void close() {
    stop();
  }

  private void closeInboundJournal() {
    this.tailer.close();
    try {
      this.inboundJournal.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static class SubmissionTask implements Runnable {

    private final ExcerptTailer tailer;
    private final Message message;

    public SubmissionTask(ExcerptTailer tailer) {
      this.tailer = tailer;
      this.message = new Message();
    }


    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        if (this.tailer.nextIndex()) {
          this.message.readMarshallable(this.tailer);
          System.out.println(this.message.getData());
        }
      }
    }

  }
}
