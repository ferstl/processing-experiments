package com.github.ferstl.processing;

import java.time.Duration;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

public class Main {

  public static void main(String[] args) throws Exception {
    // Specify the size of the ring buffer, must be power of 2.
    int bufferSize = 1024;

    // Construct the Disruptor
    Disruptor<Message> disruptor = new Disruptor<>(Message::new, bufferSize, DaemonThreadFactory.INSTANCE);

    JournalingService journalingService = new JournalingService();
    // Connect the handler
    disruptor.handleEventsWith((message, sequence, endOfBatch) -> journalingService.writeMessage(message));

    // Start the Disruptor, starts all threads running
    // Get the ring buffer from the Disruptor to be used for publishing.
    RingBuffer<Message> ringBuffer = disruptor.start();

    MessageService messageService = new MessageService();
    MessageProducer messageProducer = new MessageProducer(messageService);

    long startTime = System.currentTimeMillis();
    for (long l = 0; l < 10000; l++) {
      ringBuffer.publishEvent((event, sequence, producer) -> {
        producer.producePayment(event);
      }, messageProducer);
    }

    disruptor.shutdown();
    Duration duration = Duration.ofMillis(System.currentTimeMillis() - startTime);
    journalingService.close();

    System.out.println("Processing took " + duration);
  }
}
