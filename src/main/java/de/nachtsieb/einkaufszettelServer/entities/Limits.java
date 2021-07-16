package de.nachtsieb.einkaufszettelServer.entities;

public final class Limits {

  // Maximum amount of items inside an EInkaufszettel, also max of ordinal
  public final static int MAX_ITEMS = 128;

  // Maximum amount of a things you want to buy
  public final static int MAX_AMOUNT = Integer.MAX_VALUE;

  // The max size a thing has.
  public final static float MAX_SIZE = 32768;

  public final static String EZ_NAME_REGEX = "^.{2,48}$";

  public final static String ITEM_NAME_REGEX = "^.{2,48}$";
  public final static String ITEM_UNIT_REGEX = "^.{1,12}$";

  public final static String CAT_DESCRIPTION_REGEX = "^.{2,64}$";
  public final static String CAT_COLOR_REGEX = "^[a-fA-f0-9]{6}$";

}
