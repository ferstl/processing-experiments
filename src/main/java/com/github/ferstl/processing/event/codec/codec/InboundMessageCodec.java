package com.github.ferstl.processing.event.codec.codec;

import java.util.UUID;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import com.github.ferstl.processing.event.codec.EventType;
import com.github.ferstl.processing.event.codec.InboundMessage;
import static org.agrona.BitUtil.SIZE_OF_INT;
import static org.agrona.BitUtil.SIZE_OF_LONG;

public class InboundMessageCodec implements MessageCodec<InboundMessage> {

  private static final int EVENT_TYPE_OFFSET = 0;
  private static final int CORRELATION_ID_PART1_OFFSET = EVENT_TYPE_OFFSET + EventType.INBOUND_MESSAGE.name().length() + 4;
  private static final int CORRELATION_ID_PART2_OFFSET = CORRELATION_ID_PART1_OFFSET + SIZE_OF_LONG;
  private static final int SENDER_ID_OFFSET = CORRELATION_ID_PART2_OFFSET + SIZE_OF_LONG;
  private static final int DATA_LENGTH_OFFSET = SENDER_ID_OFFSET + SIZE_OF_INT;
  private static final int DATA_OFFSET = DATA_LENGTH_OFFSET + SIZE_OF_INT;

  @Override
  public int encode(InboundMessage event, MutableDirectBuffer buffer, int offset) {
    byte[] data = event.getData();

    buffer.putStringAscii(offset, EventType.INBOUND_MESSAGE.name());
    buffer.putLong(offset + CORRELATION_ID_PART1_OFFSET, event.getCorrelationId().getMostSignificantBits());
    buffer.putLong(offset + CORRELATION_ID_PART2_OFFSET, event.getCorrelationId().getLeastSignificantBits());
    buffer.putInt(offset + SENDER_ID_OFFSET, data.length);
    buffer.putInt(offset + DATA_LENGTH_OFFSET, data.length);
    buffer.putBytes(offset + DATA_OFFSET, data);

    return DATA_OFFSET + data.length;
  }

  @Override
  public InboundMessage decode(DirectBuffer buffer, int offset) {
    int dataLength = buffer.getInt(offset + DATA_LENGTH_OFFSET);
    byte[] data = new byte[dataLength];
    buffer.getBytes(DATA_OFFSET, data);

    return new InboundMessage(
        new UUID(buffer.getLong(offset + CORRELATION_ID_PART1_OFFSET), buffer.getLong(offset + CORRELATION_ID_PART2_OFFSET)),
        buffer.getInt(offset + SENDER_ID_OFFSET),
        data
    );
  }
}
