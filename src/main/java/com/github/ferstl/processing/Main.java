package com.github.ferstl.processing;

import java.time.Instant;
import java.util.UUID;
import com.github.ferstl.processing.Message.Metadata;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

public class Main {

  public static void main(String[] args) throws Exception {
    // Specify the size of the ring buffer, must be power of 2.
    int bufferSize = 1024;

    // Construct the Disruptor
    Disruptor<Message> disruptor = new Disruptor<>(Message::new, bufferSize, DaemonThreadFactory.INSTANCE);

    // Connect the handler
    disruptor.handleEventsWith((event, sequence, endOfBatch) -> System.out.println("Event: " + event));

    // Start the Disruptor, starts all threads running
    // Get the ring buffer from the Disruptor to be used for publishing.
    RingBuffer<Message> ringBuffer = disruptor.start();

    for (long l = 0; true; l++) {
      ringBuffer.publishEvent((event, sequence, buffer) -> {
        event.setMetadata(new Metadata(Instant.now(), UUID.randomUUID()));
        event.setData(new byte[0]);
      });
      Thread.sleep(1000);
    }
  }
}
