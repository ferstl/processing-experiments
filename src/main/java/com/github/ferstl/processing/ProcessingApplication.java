package com.github.ferstl.processing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ProcessingApplication {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static void main(String[] args) {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();

    try (AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext()) {
      appContext.register(ProcessingConfiguration.class);
      appContext.refresh();

      logger.info("Application started. Write 'exit' to stop it");
      waitForShutdown();
    }
  }

  private static void waitForShutdown() {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    while (true) {
      String line;
      try {
        line = reader.readLine();
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }

      if ("exit".equalsIgnoreCase(line)) {
        return;
      }
    }
  }
}
