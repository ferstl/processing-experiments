package com.github.ferstl.processing.event.codec.codec;

import java.util.UUID;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import com.github.ferstl.processing.accounting.AccountingStatus;
import com.github.ferstl.processing.event.codec.CommunicationEvent;
import com.github.ferstl.processing.event.codec.EventType;
import static org.agrona.BitUtil.SIZE_OF_LONG;

public class CommunicationEventCodec implements MessageCodec<CommunicationEvent> {


  private static final int EVENT_TYPE_OFFSET = 0;
  private static final int CORRELATION_ID_PART1_OFFSET = EVENT_TYPE_OFFSET + EventType.COMMUNICATION.name().length() + 4;
  private static final int CORRELATION_ID_PART2_OFFSET = CORRELATION_ID_PART1_OFFSET + SIZE_OF_LONG;
  private static final int STATUS_OFFSET = CORRELATION_ID_PART2_OFFSET + SIZE_OF_LONG;

  @Override
  public int encode(CommunicationEvent communicationEvent, MutableDirectBuffer buffer, int offset) {
    buffer.putStringAscii(offset, EventType.COMMUNICATION.name());
    buffer.putLong(offset + CORRELATION_ID_PART1_OFFSET, communicationEvent.getCorrelationId().getMostSignificantBits());
    buffer.putLong(offset + CORRELATION_ID_PART2_OFFSET, communicationEvent.getCorrelationId().getLeastSignificantBits());
    int statusLength = buffer.putStringAscii(offset + STATUS_OFFSET, communicationEvent.getAccountingStatus().name());

    return STATUS_OFFSET + statusLength;
  }

  @Override
  public CommunicationEvent decode(DirectBuffer buffer, int offset) {
    UUID correlationId = new UUID(buffer.getLong(offset + CORRELATION_ID_PART1_OFFSET), buffer.getLong(offset + CORRELATION_ID_PART2_OFFSET));
    AccountingStatus status = AccountingStatus.valueOf(buffer.getStringAscii(offset + STATUS_OFFSET));

    return new CommunicationEvent(correlationId, status);
  }
}
