package com.github.ferstl.processing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ProcessingApplication {

  public static void main(String[] args) {

    try (AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext()) {
      appContext.register(ProcessingConfiguration.class);
      appContext.refresh();
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

      if ("exit".equals(line)) {
        return;
      }
    }
  }
}
