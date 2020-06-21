package com.github.ferstl.processing.event.codec.codec;

import java.util.UUID;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import com.github.ferstl.processing.event.codec.EventType;
import com.github.ferstl.processing.event.codec.Reservation;
import static org.agrona.BitUtil.SIZE_OF_INT;
import static org.agrona.BitUtil.SIZE_OF_LONG;

public class ReservationCodec implements MessageCodec<Reservation> {

  private static final int EVENT_TYPE_OFFSET = 0;
  private static final int CORRELATION_ID_PART1_OFFSET = EVENT_TYPE_OFFSET + EventType.RESERVATION.name().length() + 4;
  private static final int CORRELATION_ID_PART2_OFFSET = CORRELATION_ID_PART1_OFFSET + SIZE_OF_LONG;
  private static final int DEBTOR_ACCOUNT_OFFSET = CORRELATION_ID_PART2_OFFSET + SIZE_OF_LONG;
  private static final int CREDITOR_ACCOUNT_OFFSET = DEBTOR_ACCOUNT_OFFSET + SIZE_OF_INT;
  private static final int AMOUNT_OFFSET = CREDITOR_ACCOUNT_OFFSET + SIZE_OF_INT;

  @Override
  public int encode(Reservation event, MutableDirectBuffer buffer, int offset) {
    buffer.putStringAscii(offset, EventType.RESERVATION.name());
    buffer.putLong(offset + CORRELATION_ID_PART1_OFFSET, event.getCorrelationId().getMostSignificantBits());
    buffer.putLong(offset + CORRELATION_ID_PART2_OFFSET, event.getCorrelationId().getLeastSignificantBits());
    buffer.putInt(offset + DEBTOR_ACCOUNT_OFFSET, event.getDebtorAccount());
    buffer.putInt(offset + CREDITOR_ACCOUNT_OFFSET, event.getCreditorAccount());
    buffer.putLong(offset + AMOUNT_OFFSET, event.getAmount());

    return AMOUNT_OFFSET + SIZE_OF_LONG;
  }

  @Override
  public Reservation decode(DirectBuffer buffer, int offset) {
    return new Reservation(
        new UUID(buffer.getLong(offset + CORRELATION_ID_PART1_OFFSET), buffer.getLong(offset + CORRELATION_ID_PART2_OFFSET)),
        buffer.getInt(offset + DEBTOR_ACCOUNT_OFFSET),
        buffer.getInt(offset + CREDITOR_ACCOUNT_OFFSET),
        buffer.getLong(offset + AMOUNT_OFFSET)
    );
  }
}
