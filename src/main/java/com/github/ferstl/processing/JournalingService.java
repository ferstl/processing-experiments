package com.github.ferstl.processing;

import java.nio.file.Paths;
import java.time.ZoneId;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;

public class JournalingService implements AutoCloseable {

  private final SingleChronicleQueue queue;

  public JournalingService() {
    this.queue = SingleChronicleQueueBuilder.single(Paths.get("target", "data").toFile())
        .build();
  }

  public void writeMessage(Message message) {
    ExcerptAppender appender = this.queue.acquireAppender();
    appender.writeDocument(wire -> wire
        .write("created").zonedDateTime(message.getMetadata().getCreationStamp().atZone(ZoneId.systemDefault()))
        .write("uuid").writeString(message.getMetadata().getUuid().toString())
        .write("message").array(message.getData(), message.getData().length));

  }

  @Override
  public void close() {
    System.out.println(this.queue.dump());
    this.queue.close();
  }
}
