package com.github.ferstl.processing.event.codec;

import java.util.UUID;
import org.agrona.ExpandableArrayBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.github.ferstl.processing.accounting.AccountingResult;
import com.github.ferstl.processing.event.codec.codec.AccountingResultCodec;
import static com.github.ferstl.processing.accounting.AccountingStatus.RESERVATION_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountingResultCodecTests {

  private AccountingResultCodec codec;
  private ExpandableArrayBuffer buffer;

  @BeforeEach
  void beforeEach() {
    this.codec = new AccountingResultCodec();
    this.buffer = new ExpandableArrayBuffer();
  }

  @Test
  void serializeAndDeserialize() {
    UUID correlationId = UUID.randomUUID();
    AccountingResult result = new AccountingResult(correlationId, RESERVATION_OK);
    this.codec.encode(result, this.buffer, 42);

    AccountingResult deserialized = this.codec.decode(this.buffer, 42);

    assertEquals(correlationId, deserialized.getCorrelationId());
    assertEquals(RESERVATION_OK, deserialized.getAccountingStatus());
  }

  @Test
  void getEncodedLength() {
    AccountingResult result = new AccountingResult(UUID.randomUUID(), RESERVATION_OK);

    assertEquals(34, this.codec.encode(result, this.buffer, 0));
  }
}
