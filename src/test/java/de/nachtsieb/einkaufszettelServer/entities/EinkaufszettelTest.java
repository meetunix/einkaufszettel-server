package de.nachtsieb.einkaufszettelServer.entities;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.nachtsieb.einkaufszettelServer.TestUtils;
import de.nachtsieb.einkaufszettelServer.exceptions.EZException;


public class EinkaufszettelTest {

	Einkaufszettel ez01;
	String nameEz01 = "ez01";
	long tsCreated;
	long tsModified;
	int currVersion;
	int newVersion;
	
	@Before // before each test
	public void setUp() throws Exception {
		ez01 = new Einkaufszettel("ez01");
		tsCreated = ez01.getCreated();
		tsModified = System.currentTimeMillis() + 1000;
		
		ez01.setItems(TestUtils.genItemList(128));
	}

	
	/***********
	 *  TESTS  *
	 ***********/
	
	// just testing correct name attribute
	@Test
	public void EZName() {
		assertThat(ez01.getName(), is(equalTo(nameEz01)));
	}
	
	// testing if exception is thrown while adding invalid name to an ez instance
	@Test
	public void EZNameException () {
		String umlauts = "ä";
		String[] names = {
				"",
				"a",
				umlauts.repeat(49),
				" ",
				"   ",
				"\n",
				null};

		for (String name : names) {
			try {
				ez01.setName(name);
				fail();
			} catch (EZException e) {
				assertThat(e, is(instanceOf(EZException.class)));
			}
		}
	}
	
	// testing if times are created properly
	@Test
	public void EZTimestampCreate () throws EZException {
		
		assertThat(ez01.getCreated(), is(equalTo(tsCreated)));
		
		// check if no newer value is accepted as creation time
		long ts = System.currentTimeMillis() - 10000;
		ez01.setCreated(tsCreated);
		assertThat(ez01.getCreated(),is(equalTo(tsCreated)));
		
		//check if a latter time is accepted
		ts = System.currentTimeMillis() + 10000;
		ez01.setCreated(ts);
		assertThat(ez01.getCreated(),is(equalTo(ts)));
	
		// test alternate constructor
		Einkaufszettel ez = new Einkaufszettel(null, tsCreated, 0, "some name", 0);
		assertThat(ez.getCreated() - (ez.getModified()),is(equalTo(0L)));
	}

	// tests if timestamps are modified properly
	@Test
	public void EZTimestampModify () {
		
		ez01.setModified(tsModified);
		assertThat(ez01.getModified(), is(equalTo(tsModified)));
		
		// modified timestamp is to early
		long ts = System.currentTimeMillis() - 10000;
		tsModified = System.currentTimeMillis();
		ez01.setModified(ts);
		assertThat(ez01.getModified() - tsModified, is(equalTo(0L)));

	}

	// tests if wrong versions are added, the version is set to one
	@Test
	public void EZVersion () throws EZException {

		int[] versions = { 0, -100, Integer.MAX_VALUE + 1};
		Einkaufszettel ez;
		
		for(int version : versions) {

			ez = new Einkaufszettel(null, 0, 0, "some name", version);
			assertThat(ez.getVersion(),is(1));
		}
	}
	

}
