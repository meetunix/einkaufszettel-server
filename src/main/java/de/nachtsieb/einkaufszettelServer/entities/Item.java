package de.nachtsieb.einkaufszettelServer.entities;

import java.util.UUID;

import de.nachtsieb.einkaufszettelServer.exceptions.EZException;
import joptsimple.internal.Strings;

public class Item {
	
	public static final int DEFAULT_ORDINAL = 1;
	public static final int DEFAULT_AMOUNT = 1;
	public static final float DEFAULT_SIZE = 1.0f;
	public static final String DEFAULT_UNIT = "Stück";
	
	private UUID iid;
	private String itemName;
	private int ordinal;
	private int amount;
	private float size;
	private String unit;
	
	// category the item belongs to
	private UUID	cid;
	private String	catDescription;
	private String	catColor;
	
	/**
	 * Default constructor used for creating new items. Default category is asumed.
	 * 
	 * @param name - String
	 * @throws EZException 
	 */
	
	public Item (String name) {
		
		this.setIid(UUID.randomUUID());
		this.setItemName(name);
		this.setOrdinal(DEFAULT_ORDINAL);
		this.setAmount(DEFAULT_AMOUNT);
		this.setCid(Category.DEFAULT_CID);
		this.setCatDescription(Category.DEFAULT_DESCRIPTION);
		this.setCatColor(Category.DEFAULT_COLOR);
		this.setSize(DEFAULT_SIZE);
		this.setUnit(DEFAULT_UNIT);
	}

	public Item (String name, Category cat) {

		this.setIid(UUID.randomUUID());
		this.setItemName(name);
		this.setOrdinal(DEFAULT_ORDINAL);
		this.setAmount(DEFAULT_AMOUNT);
		this.setCid(cat.getCid());
		this.setCatDescription(cat.getDescription());
		this.setCatColor(cat.getColor());
		this.setSize(DEFAULT_SIZE);
		this.setUnit(DEFAULT_UNIT);
	}
	
	Item () {};
	
	/**
	 * Constructor for an already existing item in the database.
	 * 
	 * @param iid 		- a UUID identifying the object in database
	 * @param itemName 	- a String which represents the name
	 * @param ordinal 	- int used for ordering inside the einkaufszettel
	 * @param amount 	- int amount of the item
	 * @param size 		- float
	 * @param unit		- String
	 * @param cat		- The category the item belongs to 
	 * @throws EZException
	 */
	public Item (
			UUID iid, 
			String itemName, 
			int ordinal, 
			int amount, 
			float size, 
			String unit,
			Category cat
			) throws EZException {
		
		this.setIid(iid);
		this.setItemName(itemName);
		this.setOrdinal(ordinal);
		this.setAmount(amount);
		this.setSize(size);
		this.setUnit(unit);		// YES, a rain bottle of whiskey is possible
		this.setCategoryValues(cat);
		
	}
	
	/**
	 * This method imports the attributes from a category to the item.
	 * 
	 * @param cat Category which values are copied to the item
	 */
	
	public void setCategoryValues (Category cat) {
		this.catColor = cat.getColor();
		this.catDescription = cat.getDescription();
		this.cid = cat.getCid();
	}
	
	/*
	 * getter and setter
	 */
	
	public UUID getIid() {
		return iid;
	}

	public void setIid(UUID iid2) {
		this.iid = iid2;
	}

	public String getItemName() {
		return itemName;
	}

	/**
	 * Sets the item name. 
	 * 
	 * @param itemName - String
	 * @throws EZException
	 */
	public void setItemName(String name) throws EZException {
		
		if (name == null || name.isBlank() || ! name.matches(Limits.ITEM_NAME_REGEX)) {
			System.out.println(name);
			throw new EZException ("Invalid name for item given.");
		} else {
			this.itemName = name;
		}
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) throws EZException {
		
		if (amount > Limits.MAX_AMOUNT || amount < 1) {
			throw new EZException("Amount is out of range.");
		} else {
			this.amount = amount;
		}
	}

	public float getSize() {
		return size;
	}

	public void setSize(float size) throws EZException {
		if (size > Limits.MAX_SIZE || size < 0) {
			throw new EZException("size is out of range");
		} else {
			this.size = size;
		}
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) throws EZException {
		
		if ( ! unit.matches(Limits.ITEM_UNIT_REGEX)) {
			throw new EZException("unit description is too long");
		} else {
			this.unit = unit;
		}
	}

	public int getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(int ordinal) throws EZException {
		
		if (ordinal > Limits.MAX_ITEMS || ordinal < 1) {
			throw new EZException("oridnal is out of range");
		} else { 
			this.ordinal = ordinal;
		}
	}

	public UUID getCid() {
		return cid;
	}

	public void setCid(UUID defaultCid) {
		this.cid = defaultCid;
	}

	public String getCatDescription() {
		
		return catDescription;
	}

	public void setCatDescription(String catDescription) throws EZException {
		
		if ( ! catDescription.matches(Limits.CAT_DESCRIPTION_REGEX)) {
			throw new EZException("category description violates constraints");
		} else {
			this.catDescription = catDescription;
		}
	}

	public String getCatColor() {
		return catColor;
	}

	public void setCatColor(String catColor) throws EZException {

		if ( ! catColor.matches(Limits.CAT_COLOR_REGEX)) {
			throw new EZException("category color violates constraints");
		} else {
			this.catColor = catColor;
		}
	}
	
	/**
	 * Compares the item with another item.
	 * 
	 * @param item
	 * @return true if they share the same values, otherwise false.
	 */

	public boolean equals(Item item) {
		float epsilon = 0.000001f;
		
		if ( 		this.iid.compareTo(item.iid) == 0
				&&	this.itemName.equals(item.getItemName())
				&&	this.ordinal == item.getOrdinal()
				&&	this.amount == item.getAmount() 
				&&	this.cid.compareTo(item.getCid()) == 0
				&&	Math.abs(this.size - item.getSize()) < epsilon
				&&	this.unit.equals(item.getUnit())
				&&	this.catColor.equalsIgnoreCase(item.catColor)
				&&	this.catDescription.equalsIgnoreCase(catDescription)
				) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		String fence = Strings.repeat('=', 31);
		
		StringBuilder sb = new StringBuilder(fence);
		sb.append(String.format("\n%-10s %20d\n","Item-Id:" , iid));
		sb.append(String.format("%-10s %20s\n","Item-Name:", itemName));
		sb.append(String.format("%-10s %20s\n","oridnal:", ordinal));
		sb.append(String.format("%-10s %20d\n","amount:", amount));
		sb.append(String.format("%-10s %20f\n","size:", size));
		sb.append(String.format("%-10s %20s\n","unit:", unit));
		sb.append(String.format("%-10s %20d\n","Cat-ID:", cid));
		sb.append(String.format("%-10s %20s\n","Cat-Descr:", catDescription));
		sb.append(String.format("%-10s %20s\n","Cat-Color:", catColor));
		sb.append(fence + "\n");
		
		return sb.toString();
	}
}
