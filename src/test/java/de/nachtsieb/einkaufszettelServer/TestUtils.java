package de.nachtsieb.einkaufszettelServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.nachtsieb.einkaufszettelServer.entities.Category;
import de.nachtsieb.einkaufszettelServer.entities.Einkaufszettel;
import de.nachtsieb.einkaufszettelServer.entities.Item;
import de.nachtsieb.einkaufszettelServer.entities.Limits;
import de.nachtsieb.einkaufszettelServer.exceptions.EZException;

public final class TestUtils {
	
	public static Einkaufszettel genRandomEZ() {
		
		Einkaufszettel ez = new Einkaufszettel("Random EZ " + getRandomInteger(100,999));
		ez.setItems(genItemList(32));
		
		return ez;
		
	}
	
	public static Category genRandomCategory() {
		Category cat = new Category("CAFFEE", "Random Category " + getRandomInteger(100, 999));
		return cat;
	}
	
	public static Item genRandomItem() throws EZException {
		Item it = new Item("fancy item " + getRandomInteger(100, 999)); 
				it.setOrdinal(getRandomInteger(1, Limits.MAX_ITEMS));
				it.setAmount(getRandomInteger(1, Limits.MAX_AMOUNT));
				it.setSize(getRandomFloat(0, Limits.MAX_SIZE));
				it.setCategoryValues(new Category());
		return it;
	}
	
	public static List<Item> getRandomItemListWithRandomCategory(int len) {
		
		List<Item> itemList = new ArrayList<>();
		for (int i = 1; i <= len ; i++) {
			Item item = genRandomItem();
			item.setCategoryValues(genRandomCategory());
			itemList.add(item);
		}
		return itemList;
	}
	
	public static List<Item> genItemList(int len) throws EZException {
		List<Item> itemList = new ArrayList<>();
		for (int i = 1; i <= len ; i++) {
			Item item = genRandomItem();
			itemList.add(item);
		}
		return itemList;
	}
	/**
	 * Returns a random long (n) within [x,z].
	 * 
	 * @param x; long
	 * @param y long
	 * @return long with x <= n < z
	 */
	public static long getRandomLong (long x, long y) {
		return x + (long) (Math.random() * (y - x));
	}
	
	/**
	 * Returns a random positive long
	 * 
	 * @return a long where 1 <= x <= (2^63 - 1)
	 */
	public static long getRandomLong () {
		return 1 + (long) (Math.random() * ( Long.MAX_VALUE ));
	}
	
	/**
	 * Returns a random int (n) within a given range [x,y[
	 * 
	 * @param x int min value
	 * @param y int max value
	 * @return int with x <= n < y
	 */
	public static int getRandomInteger(int x, int y) {
		return x + (int) (new Random().nextFloat() * (y - x));
	}
	
	
	/**
	 * Returns a random positive integer.
	 * 
	 * @return a int where 1 <= x <= (2^31 - 1)
	 */
	public static int getRandomInteger () {
		return 1 + (int) (new Random().nextFloat() * (Integer.MAX_VALUE));
	}
	
	/**
	 * Returns a random float (n) within x and z.
	 * 
	 * @param x; long
	 * @param y long
	 * @return long with x <= n <= y
	 */
	public static float getRandomFloat (float x, float y) {
		return x + new Random().nextFloat() * ( y - x);
	}
	
	/**
	 * 
	 * Returns a random positive float (n >= 0).
	 * 
	 * @return float where 0 <= n <= Float.MAX_Value
	 */
	
	public static float getRandomFloat () {
		return new Random().nextFloat() * ( Float.MAX_VALUE);
	}
}
