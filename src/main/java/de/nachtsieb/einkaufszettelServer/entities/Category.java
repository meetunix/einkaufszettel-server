package de.nachtsieb.einkaufszettelServer.entities;

import java.util.UUID;

public class Category {

  public static final UUID DEFAULT_CID = UUID.fromString("cd70a281-9a75-4582-b2be-76b32cb6928c");
  public static final String DEFAULT_COLOR = "000000";
  public static final String DEFAULT_DESCRIPTION = "default";

  private UUID cid;
  private String color;
  private String description;

  public Category(UUID cid, String color, String description) {
    setCid(cid);
    setColor(color);
    setDescription(description);
  }

  public Category(String color, String description) {
    this(UUID.randomUUID(), color, description);
  }

  public Category() {
    this(UUID.randomUUID(), DEFAULT_COLOR, DEFAULT_DESCRIPTION);
  }

  static Category getDefaultCategory() {
    return new Category(DEFAULT_CID, DEFAULT_COLOR, DEFAULT_DESCRIPTION);
  }

  public boolean equals(Category cat) {
    return this.color.equalsIgnoreCase(cat.getColor())
        && this.description.equalsIgnoreCase(cat.getDescription())
        && this.cid.compareTo(cat.getCid()) == 0;
  }

  /*
   * getter and setter
   */
  public UUID getCid() {
    return cid;
  }

  public void setCid(UUID cid) {
    this.cid = cid;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
