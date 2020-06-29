package com.github.ferstl.processing.cluster;

public final class PortUtil {

  private static final int PORT_BASE = 9000;
  private static final int PORTS_PER_NODE = 100;

  private PortUtil() {
    throw new AssertionError("not instantiable");
  }

  public static int calculatePort(int nodeId, int offset) {
    return PORT_BASE + (nodeId * PORTS_PER_NODE) + offset;
  }
}
