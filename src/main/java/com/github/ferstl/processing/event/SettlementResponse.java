package com.github.ferstl.processing.event;

import java.util.UUID;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import static org.agrona.BitUtil.SIZE_OF_BYTE;
import static org.agrona.BitUtil.SIZE_OF_LONG;

public class SettlementResponse {

  private static final int CORRELATION_ID_PART1_OFFSET = 0;
  private static final int CORRELATION_ID_PART2_OFFSET = CORRELATION_ID_PART1_OFFSET + SIZE_OF_LONG;
  private static final int IS_SETTLED_OFFSET = CORRELATION_ID_PART2_OFFSET + SIZE_OF_LONG;


  private final UUID correlationId;
  private final boolean isSettled;

  public SettlementResponse(UUID correlationId, boolean isSettled) {
    this.correlationId = correlationId;
    this.isSettled = isSettled;
  }

  public static SettlementResponse deserialize(DirectBuffer buffer, int offset) {
    UUID correlationId = new UUID(buffer.getLong(offset + CORRELATION_ID_PART1_OFFSET), buffer.getLong(offset + CORRELATION_ID_PART2_OFFSET));
    boolean isSettled = buffer.getByte(offset + IS_SETTLED_OFFSET) != 0;

    return new SettlementResponse(correlationId, isSettled);
  }

  public void serialize(MutableDirectBuffer buffer, int offset) {
    buffer.putLong(offset + CORRELATION_ID_PART1_OFFSET, getCorrelationId().getMostSignificantBits());
    buffer.putLong(offset + CORRELATION_ID_PART2_OFFSET, getCorrelationId().getLeastSignificantBits());
    buffer.putByte(offset + IS_SETTLED_OFFSET, this.isSettled ? (byte) 1 : (byte) 0);
  }


  public UUID getCorrelationId() {
    return this.correlationId;
  }

  public boolean isSettled() {
    return this.isSettled;
  }


  public int getSerializedLength() {
    return IS_SETTLED_OFFSET + SIZE_OF_BYTE;
  }
}
