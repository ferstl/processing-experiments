package com.github.ferstl.processing.event.codec.codec;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

public interface MessageCodec<T> {

  T decode(DirectBuffer buffer, int offset);

  int encode(T event, MutableDirectBuffer buffer, int offset);
}
