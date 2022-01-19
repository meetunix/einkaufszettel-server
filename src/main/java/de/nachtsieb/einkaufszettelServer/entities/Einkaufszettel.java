package de.nachtsieb.einkaufszettelServer.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlRootElement;
import de.nachtsieb.einkaufszettelServer.exceptions.EZException;


/**
 * This class represents the Einkaufszettel and the items that belongs to it.
 */
@XmlRootElement
public class Einkaufszettel {


  private UUID eid; // the pkey of this relation JDBC: .setObject() and .getObject()
  private long created;
  private long modified;
  private String name;
  private int version;
  private List<Item> items;

  /**
   * Constructor for an existing Einkaufszettel.
   * 
   * @param eid UUID
   * @param created long
   * @param modified long
   * @param name String
   * @param version int
   * @throws EZException
   */

  public Einkaufszettel(UUID eid, long created, long modified, String name, int version)
      throws EZException {

    this.setEid(eid);
    this.setCreated(created);
    this.setModified(modified);
    this.setName(name);
    this.setVersion(version);
    this.items = null;
  }

  /**
   * Constructor for a new Einkaufszettel.
   * 
   * @param name
   * @throws EZException
   */
  public Einkaufszettel(String name) throws EZException {

    this.setEid(null);
    this.setCreated(0);
    this.setModified(0);
    this.setName(name);
    this.setVersion(0);
    this.items = null;
  }

  public Einkaufszettel() {};


  /********************
   * additional methods
   ********************/


  /**
   * Compares the eid from the Object with a given UUID.
   * 
   * @param otherEid - UUID
   * @return true if the eid of the Object has the same value as otherEid.
   */

  public boolean istTheSame(UUID otherEid) {
    return this.eid.equals(otherEid);
  }

  /**
   * Adds a new item to the list of items for this einkaufszettel
   * 
   * @param item - Item
   * @return true if value was added succesfull
   */

  public boolean addItem(Item item) {

    // if no initial item list was set, this is the right time to set empty list
    if (this.items == null) {
      this.items = new ArrayList<>();
    }

    return items.add(item);

  }

  /**
   * Increments the version
   */
  public void incrementVersion() {
    if (version < Integer.MAX_VALUE) {
      this.version += 1;
    } else {
      this.version = 1;
    }
  }


  /*******************
   * getter and setter
   *******************/

  public UUID getEid() {
    return eid;
  }


  /**
   * Sets the eid, if null is given a new UUID is generated.
   * 
   * @param eid a UUID or null
   */

  public void setEid(UUID eid) throws EZException {

    if (eid == null) {
      this.eid = UUID.randomUUID();
    } else {
      this.eid = eid;
    }

  }


  public long getCreated() {
    return created;
  }

  /**
   * Sets the time of creation. If 0 ist given, the current time will be used.
   * 
   * @param created long
   */
  public void setCreated(long created) {

    if (created == 0) {
      this.created = System.currentTimeMillis();
    } else {
      this.created = created;
    }

  }


  public long getModified() {
    return modified;
  }


  /**
   * Sets the time of last modification. If 0 is given the creation time is used. If the given time
   * is smaller the current time is used.
   * 
   * @param modified - long
   */
  public void setModified(long modified) {

    if (modified == 0) {
      this.modified = this.created;
    } else if (modified < this.modified) {
      this.modified = System.currentTimeMillis();
    } else {
      this.modified = modified;
    }
  }


  public String getName() {
    return name;
  }

  /**
   * Sets a name for the einkaufszettel.
   * 
   * @param name - String
   * @throws EZException
   */

  public void setName(String name) throws EZException {

    if (name == null || name.isBlank() || !name.matches(Limits.EZ_NAME_REGEX)) {
      throw new EZException("Empty or invalid name for einkaufszettel.");
    } else {
      this.name = name;
    }
  }


  public int getVersion() {
    return version;
  }


  /**
   * Sets the current version of the einkaufszettel, if 0 or negative value given the version is set
   * to 1.
   * 
   * @param version - int
   */

  public void setVersion(int version) {

    if (version < 1 || version > Integer.MAX_VALUE) {
      this.version = 1;
    } else {

      this.version = version;
    }
  }


  public List<Item> getItems() {
    return items;
  }


  public void setItems(List<Item> items) throws EZException {

    if (items.size() > Limits.MAX_ITEMS) {
      throw new EZException("too much items in items list");
    } else {
      this.items = items;
    }
  }

  public boolean equals(Einkaufszettel ez) {

    boolean ezCheck = this.eid.compareTo(ez.getEid()) == 0 && this.created == ez.getCreated()
        && this.modified == ez.getModified() && this.name.equals(ez.getName())
        && this.version == ez.getVersion() && this.items.size() == ez.getItems().size();

    if (!ezCheck) {
      return false;
    }

    // compare all items
    return compareItemList(this.getItems());

  }

  private boolean compareItemList(List<Item> items) {

    // create map from this EZ item list for faster comparison
    Map<UUID, Item> itemMap =
        this.items.stream().collect(Collectors.toMap(Item::getIid, item -> item));

    return itemMap.size() == items.stream().filter(i -> i.equals(itemMap.get(i.getIid())))
        .collect(Collectors.toList()).size();
  }

  @Override
  public String toString() {

    int amountOfItems;
    if (items != null) {
      amountOfItems = items.size();
    } else {
      amountOfItems = 0;
    }

    String equal = "=";
    String fence = equal.repeat(51);

    StringBuilder sb = new StringBuilder(fence);
    sb.append(String.format("\n%-10s %40s\n", "EZ-Id:", eid));
    sb.append(String.format("%-10s %40s\n", "Name:", name));
    sb.append(String.format("%-10s %40d\n", "version:", version));
    sb.append(String.format("%-10s %40s\n", "created:", created));
    sb.append(String.format("%-10s %40s\n", "modified:", modified));
    sb.append(String.format("%-10s %40d\n", "#Items:", amountOfItems));
    sb.append(fence + "\n");

    return sb.toString();
  }
}
