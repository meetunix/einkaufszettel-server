package de.nachtsieb.einkaufszettelServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RessourceLoader {

  public RessourceLoader() {}

  // get a file from the resources' directory (works inside and outside jar file)
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
  public String getStringFromInputStream(InputStream is) {

    StringBuilder sb = new StringBuilder();
    BufferedReader br = new BufferedReader(new InputStreamReader(is));

    try {

      String line;
      while ((line = br.readLine()) != null) sb.append(line);

    } catch (IOException e) {
      e.printStackTrace();
    }

    return sb.toString();
  }
}
