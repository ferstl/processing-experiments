package com.github.ferstl.processing.cluster;

import org.agrona.DirectBuffer;
import com.github.ferstl.processing.accounting.AccountingResult;
import com.github.ferstl.processing.accounting.AccountingService;
import com.github.ferstl.processing.event.codec.EventType;
import com.github.ferstl.processing.event.codec.Reservation;
import com.github.ferstl.processing.event.codec.codec.ReservationCodec;

public class EventDispatcher {

  private final ReservationCodec reservationCodec;
  private final AccountingService accountingService;

  public EventDispatcher(AccountingService accountingService) {
    this.accountingService = accountingService;
    this.reservationCodec = new ReservationCodec();
  }


  public AccountingResult dispatch(DirectBuffer buffer, int offset) {
    String eventTypeName = buffer.getStringAscii(offset);
    EventType eventType = EventType.valueOf(eventTypeName);

    switch (eventType) {
      case RESERVATION:
        Reservation reservation = this.reservationCodec.decode(buffer, offset);
        System.out.println("-> Received reservation " + reservation.getCorrelationId());
        return this.accountingService.reserve(reservation);

      default:
        throw new IllegalArgumentException("Unknown event type: " + eventType);
    }
  }
}
