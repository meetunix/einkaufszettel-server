package de.nachtsieb.einkaufszettelServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ResLoader {

  public ResLoader() {}

  // get a file from the resources directory (works inside and outside jar file)
  public InputStream getFileFromResourceAsStream(String fileName) {

    ClassLoader classLoader = getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(fileName);

    if (inputStream == null) {
      throw new IllegalArgumentException("file not found! " + fileName);
    } else {
      return inputStream;
    }
  }

  // convert input stream to string
  public String getStringfromInputstream(InputStream is) {

    StringBuilder sb = new StringBuilder();
    BufferedReader br = new BufferedReader(new InputStreamReader(is));

    try {

      String line;
      while ((line = br.readLine()) != null)
        sb.append(line);

    } catch (IOException e) {
      e.printStackTrace();
    }

    return sb.toString();

  }

  // print input stream (just for debugging)
  public void printInputStream(InputStream is) {

    try (InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader)) {

      String line;
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
