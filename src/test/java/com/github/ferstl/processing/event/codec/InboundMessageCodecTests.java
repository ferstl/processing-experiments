package com.github.ferstl.processing.event.codec;

import java.util.UUID;
import org.agrona.ExpandableArrayBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.github.ferstl.processing.event.codec.codec.InboundMessageCodec;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InboundMessageCodecTests {

  private static final byte[] DATA = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
  private InboundMessageCodec codec;
  private ExpandableArrayBuffer buffer;

  @BeforeEach
  void beforeEach() {
    this.codec = new InboundMessageCodec();
    this.buffer = new ExpandableArrayBuffer();
  }

  @Test
  void serializeAndDeserialize() {
    UUID correlationId = UUID.randomUUID();
    InboundMessage event = new InboundMessage(correlationId, 12345, DATA);

    this.codec.encode(event, this.buffer, 42);

    InboundMessage deserialized = this.codec.decode(this.buffer, 42);

    assertEquals(correlationId, deserialized.getCorrelationId());
    assertEquals(12345, event.getSenderId());
    assertArrayEquals(DATA, event.getData());
  }

  @Test
  void getEncodedLength() {
    InboundMessage event = new InboundMessage(UUID.randomUUID(), 12345, DATA);
    assertEquals(53, this.codec.encode(event, this.buffer, 0));
  }
}
