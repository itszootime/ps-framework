package org.uncertweb.test.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.uncertweb.ps.data.Input;
import org.uncertweb.ps.data.MultipleInput;
import org.uncertweb.ps.data.ProcessInputs;
import org.uncertweb.ps.data.SingleInput;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * These were originally created for testing, but they are probably useful elsewhere too.
 * 
 * @author Richard Jones
 *
 */
public class TestUtils {
	
	public static Document loadXML(String path) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		builder.setIgnoringBoundaryWhitespace(true);
		return builder.build(TestUtils.class.getClassLoader().getResourceAsStream(path));
	}
	
	public static JsonObject loadJSON(String path) throws IOException {
		JsonParser parser = new JsonParser();
		InputStream is = TestUtils.class.getClassLoader().getResourceAsStream(path);
		try (InputStreamReader isr = new InputStreamReader(is)) {
			return (JsonObject)parser.parse(isr);
		}
	}
	
	public static InputStream streamFor(String path) {
		return TestUtils.class.getClassLoader().getResourceAsStream(path);
	}
	
	public static DateMidnight createDateMidnight(int year, int month, int day, String timezone) {
		return new DateMidnight(new DateTime(year, month, day, 0, 0, 0, 0, DateTimeZone.forID(timezone)));
	}
	
	public static void testSingleInput(ProcessInputs inputs, String identifier, Object expected) {
		Input input = inputs.get(identifier); 
		assertNotNull(input);
		assertEquals(identifier, input.getIdentifier());
		assertTrue(input instanceof SingleInput);
		Object actual = input.getAsSingleInput().getObjectAs(Object.class);
		assertEquals(expected, actual);
	}

	public static void testMultipleInput(ProcessInputs inputs, String identifier, Object[] expected) {
		Input input = inputs.get(identifier);
		assertNotNull(input);
		assertEquals(identifier, input.getIdentifier());
		assertTrue(input instanceof MultipleInput);
		Object[] actual = input.getAsMultipleInput().getObjectsAs(Object.class).toArray(new Object[0]);
		assertArrayEquals(expected, actual);
	}

}
