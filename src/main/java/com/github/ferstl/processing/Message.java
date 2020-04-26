package com.github.ferstl.processing;

import java.time.Instant;
import java.util.UUID;

public class Message {

  private Metadata metadata;
  private byte[] data;

  public void setData(byte[] data) {
    this.data = data;
  }

  public byte[] getData() {
    return this.data;
  }

  public Metadata getMetadata() {
    return this.metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  public void clear() {
    this.metadata = null;
    this.data = null;
  }

  public static class Metadata {

    private final Instant creationStamp;
    private final UUID uuid;

    public Metadata(Instant creationStamp, UUID uuid) {
      this.creationStamp = creationStamp;
      this.uuid = uuid;
    }

    public Instant getCreationStamp() {
      return this.creationStamp;
    }

    public UUID getUuid() {
      return this.uuid;
    }
  }
}
