package de.nachtsieb.einkaufszettelServer.entities;

public enum Patterns {
  EZ_NAME("^.{2,48}$"),
  ITEM_NAME("^.{2,48}$"),
  ITEM_UNIT("^.{1,12}$"),
  CAT_DESCRIPTION("^.{2,64}$"),
  CAT_COLOR("^[a-fA-f0-9]{6}$");

  private final String regex;

  Patterns(String regex) {
    this.regex = regex;
  }

  String getRegex() {
    return regex;
  }
}
