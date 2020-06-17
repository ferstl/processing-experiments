package com.github.ferstl.processing.event;

import java.util.UUID;
import org.agrona.ExpandableArrayBuffer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationEventTests {

  @Test
  void serializeAndDeserialize() {
    UUID correlationId = UUID.randomUUID();
    ReservationEvent event = new ReservationEvent(correlationId, 1, 2, 500);

    ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();
    event.serialize(buffer, 0);

    ReservationEvent deserialized = ReservationEvent.deserialize(buffer, 0);

    assertEquals(correlationId, deserialized.getCorrelationId());
    assertEquals(1, event.getDebtorAccount());
    assertEquals(2, event.getCreditorAccount());
    assertEquals(500, event.getAmount());
  }

  @Test
  void getSerializedLength() {
    ReservationEvent event = new ReservationEvent(UUID.randomUUID(), 1, 2, 100);
    assertEquals(3, event.getSerializedLength());
  }
}
