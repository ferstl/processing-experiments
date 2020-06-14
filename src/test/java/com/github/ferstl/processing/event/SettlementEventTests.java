package com.github.ferstl.processing.event;

import java.util.UUID;
import org.agrona.ExpandableArrayBuffer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SettlementEventTests {

  @Test
  void serializeAndDeserialize() {
    UUID correlationId = UUID.randomUUID();
    SettlementEvent event = new SettlementEvent(correlationId, 1, 2, 500);

    ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();
    event.serialize(buffer, 0);

    SettlementEvent deserialized = SettlementEvent.deserialize(buffer, 0);

    assertEquals(correlationId, deserialized.getCorrelationId());
    assertEquals(1, event.getDebtorAccount());
    assertEquals(2, event.getCreditorAccount());
    assertEquals(500, event.getAmount());
  }

  @Test
  void getSerializedLength() {
    SettlementEvent event = new SettlementEvent(UUID.randomUUID(), 1, 2, 100);
    assertEquals(3, event.getSerializedLength());
  }
}
