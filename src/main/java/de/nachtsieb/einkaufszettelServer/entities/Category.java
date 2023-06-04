package de.nachtsieb.einkaufszettelServer.entities;

public class Category {

  public static final String DEFAULT_COLOR = "000000";
  public static final String DEFAULT_DESCRIPTION = "default";

  private String color;
  private String description;

  public Category(String color, String description) {
    setColor(color);
    setDescription(description);
  }

  public Category() {
    this(DEFAULT_COLOR, DEFAULT_DESCRIPTION);
  }

  static Category getDefaultCategory() {
    return new Category(DEFAULT_COLOR, DEFAULT_DESCRIPTION);
  }

  public boolean equals(Category cat) {
    return this.color.equalsIgnoreCase(cat.getColor())
        && this.description.equalsIgnoreCase(cat.getDescription());
  }

  /*
   * getter and setter
   */

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
