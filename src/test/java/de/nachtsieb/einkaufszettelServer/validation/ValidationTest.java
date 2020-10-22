package de.nachtsieb.einkaufszettelServer.validation;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.nachtsieb.einkaufszettelServer.TestUtils;
import de.nachtsieb.einkaufszettelServer.entities.Category;
import de.nachtsieb.einkaufszettelServer.entities.Einkaufszettel;
import de.nachtsieb.einkaufszettelServer.entities.Item;
import de.nachtsieb.einkaufszettelServer.exceptions.EZException;

public class ValidationTest {

	Einkaufszettel ez;
	ObjectMapper mapper;
	
	@Before
	public void setUp() throws Exception {
		ez = new Einkaufszettel("Mein Einkaufszettel");
		mapper = new ObjectMapper();
	}

	// tests if  Object -> json1 -> Object -> json2 : json1 == json2  
	@Test
	public void dataBindingTest()
			throws EZException, JsonMappingException, JsonProcessingException {
	
		Category catMilk = new Category(UUID.randomUUID(), "FF00FF", "Milchprodukt");
		Category catSpirit = new Category(UUID.randomUUID(), "E600FF", "Spirituosen");
		Item cheese = new Item(UUID.randomUUID(), "Emmentaler", 1 , 1, 200f , "g",catMilk);
		Item alc = new Item(UUID.randomUUID(), "Feiner Alter", 1 , 1, 0.7f , "l", catSpirit);
		
		ez.addItem(alc);
		ez.addItem(cheese);
	
		// object -> json1
		String json1 = mapper.writeValueAsString(ez);
		//json1 -> object
		JsonNode node = mapper.readTree(json1);
		Einkaufszettel revEz = mapper.readValue(json1, Einkaufszettel.class);
		//object -> json2
		String json2 = mapper.writeValueAsString(revEz);
		
		assertThat(json1.equals(json2), is(true));
	}
	
	@Test
	public void dataBindingTestRandom() throws EZException, JsonProcessingException {
		ez.setItems(TestUtils.genItemList(128));

		// object -> json1
		String json1 = mapper.writeValueAsString(ez);
		//json1 -> object
		JsonNode node = mapper.readTree(json1);
		Einkaufszettel revEz = mapper.readValue(json1, Einkaufszettel.class);
		//object -> json2
		String json2 = mapper.writeValueAsString(revEz);
		
		assertThat(json1.equals(json2), is(true));
		
	}
}
