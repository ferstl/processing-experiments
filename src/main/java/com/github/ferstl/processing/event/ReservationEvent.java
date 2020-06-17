package com.github.ferstl.processing.event;

import java.util.UUID;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import static org.agrona.BitUtil.SIZE_OF_INT;
import static org.agrona.BitUtil.SIZE_OF_LONG;

public class ReservationEvent extends ProcessingEvent {

  private static final int CORRELATION_ID_PART1_OFFSET = 0;
  private static final int CORRELATION_ID_PART2_OFFSET = CORRELATION_ID_PART1_OFFSET + SIZE_OF_LONG;
  private static final int DEBTOR_ACCOUNT_OFFSET = CORRELATION_ID_PART2_OFFSET + SIZE_OF_LONG;
  private static final int CREDITOR_ACCOUNT_OFFSET = DEBTOR_ACCOUNT_OFFSET + SIZE_OF_INT;
  private static final int AMOUNT_OFFSET = CREDITOR_ACCOUNT_OFFSET + SIZE_OF_INT;

  private final int debtorAccount;
  private final int creditorAccount;
  private final long amount;

  public ReservationEvent(UUID correlationId, int debtorAccount, int creditorAccount, long amount) {
    super(correlationId);
    this.debtorAccount = debtorAccount;
    this.creditorAccount = creditorAccount;
    this.amount = amount;
  }

  public static ReservationEvent deserialize(DirectBuffer buffer, int offset) {
    UUID correlationId = new UUID(buffer.getLong(offset + CORRELATION_ID_PART1_OFFSET), buffer.getLong(offset + CORRELATION_ID_PART2_OFFSET));

    return new ReservationEvent(correlationId, buffer.getInt(offset + DEBTOR_ACCOUNT_OFFSET), buffer.getInt(offset + CREDITOR_ACCOUNT_OFFSET), buffer.getLong(offset + AMOUNT_OFFSET));
  }

  public void serialize(MutableDirectBuffer buffer, int offset) {
    buffer.putLong(offset + CORRELATION_ID_PART1_OFFSET, getCorrelationId().getMostSignificantBits());
    buffer.putLong(offset + CORRELATION_ID_PART2_OFFSET, getCorrelationId().getLeastSignificantBits());
    buffer.putInt(offset + DEBTOR_ACCOUNT_OFFSET, this.debtorAccount);
    buffer.putInt(offset + CREDITOR_ACCOUNT_OFFSET, this.creditorAccount);
    buffer.putLong(offset + AMOUNT_OFFSET, this.amount);
  }

  public int getDebtorAccount() {
    return this.debtorAccount;
  }

  public int getCreditorAccount() {
    return this.creditorAccount;
  }

  public long getAmount() {
    return this.amount;
  }

  public int getSerializedLength() {
    return AMOUNT_OFFSET + SIZE_OF_LONG;
  }
}
