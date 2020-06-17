package com.github.ferstl.processing.event;

import java.util.UUID;
import org.agrona.ExpandableArrayBuffer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationEventTests {

  @Test
  void serializeAndDeserialize() {
    UUID correlationId = UUID.randomUUID();
    ReservationEvent event = new ReservationEvent(correlationId, 24798, 24799, 500);

    ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();
    event.serialize(buffer, 42);

    ReservationEvent deserialized = ReservationEvent.deserialize(buffer, 42);

    assertEquals(correlationId, deserialized.getCorrelationId());
    assertEquals(24798, event.getDebtorAccount());
    assertEquals(24799, event.getCreditorAccount());
    assertEquals(500, event.getAmount());
  }

  @Test
  void getSerializedLength() {
    ReservationEvent event = new ReservationEvent(UUID.randomUUID(), 24798, 24799, 100);
    assertEquals(32, event.getSerializedLength());
  }
}
