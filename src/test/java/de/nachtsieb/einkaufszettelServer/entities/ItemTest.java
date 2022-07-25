package de.nachtsieb.einkaufszettelServer.entities;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import de.nachtsieb.einkaufszettelServer.exceptions.EZException;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

public class ItemTest {

  Item it01;
  Item it02;

  @Before
  public void setUp() {
    it01 = new Item("Sterni");
    it02 = new Item("Sterni");
  }

  // checks the item.equals() method
  @Test
  public void itemEqualityCID() {
    it02.setCid(UUID.randomUUID());
    assertThat(it01.equals(it02), is(false));
  }

  @Test
  public void itemEqualityName() throws EZException {
    it02.setItemName("Hasseröder");
    assertThat(it01.equals(it02), is(false));
  }

  @Test
  public void itemEqualityOrdinal() throws EZException {
    it02.setOrdinal(2);
    assertThat(it01.equals(it02), is(false));
  }

  @Test
  public void itemEqualityAmount() throws EZException {
    it02.setAmount(2);
    assertThat(it01.equals(it02), is(false));
  }

  @Test
  public void itemEqualitySize() throws EZException {
    it02.setIid(it01.getIid());
    assertThat(it01.equals(it02), is(true));

    it02.setSize(1.5f);
    assertThat(it01.equals(it02), is(false));

    // difference is smaller than epsilon
    it01.setSize(1.5000000000000001f);
    assertThat(it01.equals(it02), is(true));

    // difference is bigger than epsilon
    it01.setSize(1.50001f);
    assertThat(it01.equals(it02), is(false));

    it01.setSize(0f);
    it02.setSize(-0f);
    assertThat(it01.equals(it02), is(true));
  }

  @Test
  public void itemEqualityUnit() throws EZException {
    it02.setIid(it01.getIid());

    it02.setUnit("some other");
    assertThat(it01.equals(it02), is(false));

    it01.setUnit("some other");
    assertThat(it01.equals(it02), is(true));

    it01.setUnit("some Other");
    assertThat(it01.equals(it02), is(false));
  }
}
