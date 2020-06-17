package com.github.ferstl.processing.event;

import java.util.UUID;
import org.agrona.ExpandableArrayBuffer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettlementResponseTests {

  @Test
  void serializeAndDeserialize() {
    UUID correlationId = UUID.randomUUID();
    SettlementResponse response = new SettlementResponse(correlationId, true);

    ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();
    response.serialize(buffer, 0);

    ReservationEvent deserialized = ReservationEvent.deserialize(buffer, 0);

    assertEquals(correlationId, deserialized.getCorrelationId());
    assertTrue(response.isSettled());
  }

  @Test
  void getSerializedLength() {
    SettlementResponse response = new SettlementResponse(UUID.randomUUID(), false);

    assertEquals(17, response.getSerializedLength());
  }
}
