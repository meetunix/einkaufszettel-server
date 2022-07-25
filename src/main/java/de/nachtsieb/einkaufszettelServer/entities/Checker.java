package de.nachtsieb.einkaufszettelServer.entities;

import java.util.EnumMap;
import java.util.regex.Pattern;

public final class Checker {

  private static final EnumMap<Patterns, Pattern> matcherMap =
      new EnumMap<Patterns, Pattern>(Patterns.class);

  static {
    for (Patterns p : Patterns.values()) {
      Pattern pat = Pattern.compile(p.getRegex());
      matcherMap.put(p, pat);
    }
  }

  public static boolean notMatches(Patterns pattern, String toMatch) {
    synchronized (matcherMap) {
      return !matcherMap.get(pattern).matcher(toMatch).matches();
    }
  }
}
