package com.github.ferstl.processing.model;

import java.util.UUID;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshallable;

public class Message implements BytesMarshallable {

  private UUID correlationId;
  private String data;

  public void newCorrelationId() {
    this.correlationId = UUID.randomUUID();
  }

  public UUID getCorrelationId() {
    return this.correlationId;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getData() {
    return this.data;
  }

  @Override
  public void readMarshallable(Bytes in) throws IllegalStateException {
    this.correlationId = new UUID(in.readLong(), in.readLong());
    this.data = in.readUTF();
  }

  @Override
  public void writeMarshallable(Bytes out) {
    out.writeLong(this.correlationId.getMostSignificantBits());
    out.writeLong(this.correlationId.getLeastSignificantBits());
  }

}
