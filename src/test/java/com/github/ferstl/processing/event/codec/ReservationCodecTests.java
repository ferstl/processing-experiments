package com.github.ferstl.processing.event.codec;

import java.util.UUID;
import org.agrona.ExpandableArrayBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.github.ferstl.processing.event.codec.codec.ReservationCodec;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationCodecTests {

  private ReservationCodec codec;
  private ExpandableArrayBuffer buffer;

  @BeforeEach
  void beforeEach() {
    this.codec = new ReservationCodec();
    this.buffer = new ExpandableArrayBuffer();
  }

  @Test
  void serializeAndDeserialize() {
    UUID correlationId = UUID.randomUUID();
    Reservation event = new Reservation(correlationId, 24798, 24799, 500);

    this.codec.encode(event, this.buffer, 42);

    Reservation deserialized = this.codec.decode(this.buffer, 42);

    assertEquals(correlationId, deserialized.getCorrelationId());
    assertEquals(24798, event.getDebtorAccount());
    assertEquals(24799, event.getCreditorAccount());
    assertEquals(500, event.getAmount());
  }

  @Test
  void getEncodedLength() {
    Reservation event = new Reservation(UUID.randomUUID(), 24798, 24799, 100);
    assertEquals(47, this.codec.encode(event, this.buffer, 0));
  }
}
