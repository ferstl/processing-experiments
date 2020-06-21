package com.github.ferstl.processing.event.codec.codec;

import java.util.UUID;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import com.github.ferstl.processing.accounting.AccountingResult;
import com.github.ferstl.processing.accounting.AccountingStatus;
import static org.agrona.BitUtil.SIZE_OF_LONG;

public class AccountingResultCodec implements MessageCodec<AccountingResult> {

  private static final int CORRELATION_ID_PART1_OFFSET = 0;
  private static final int CORRELATION_ID_PART2_OFFSET = CORRELATION_ID_PART1_OFFSET + SIZE_OF_LONG;
  private static final int STATUS_OFFSET = CORRELATION_ID_PART2_OFFSET + SIZE_OF_LONG;

  @Override
  public int encode(AccountingResult accountingResult, MutableDirectBuffer buffer, int offset) {
    buffer.putLong(offset + CORRELATION_ID_PART1_OFFSET, accountingResult.getCorrelationId().getMostSignificantBits());
    buffer.putLong(offset + CORRELATION_ID_PART2_OFFSET, accountingResult.getCorrelationId().getLeastSignificantBits());
    int statusLength = buffer.putStringAscii(offset + STATUS_OFFSET, accountingResult.getAccountingStatus().name());

    return STATUS_OFFSET + statusLength;
  }

  @Override
  public AccountingResult decode(DirectBuffer buffer, int offset) {
    UUID correlationId = new UUID(buffer.getLong(offset + CORRELATION_ID_PART1_OFFSET), buffer.getLong(offset + CORRELATION_ID_PART2_OFFSET));
    AccountingStatus status = AccountingStatus.valueOf(buffer.getStringAscii(offset + STATUS_OFFSET));

    return new AccountingResult(correlationId, status);
  }
}
