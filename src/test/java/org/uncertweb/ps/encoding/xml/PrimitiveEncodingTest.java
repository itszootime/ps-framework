package org.uncertweb.ps.encoding.xml;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.jdom.Content;
import org.jdom.Text;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.ParseException;
import org.uncertweb.ps.encoding.xml.AbstractXMLEncoding.Include;
import org.uncertweb.ps.encoding.xml.AbstractXMLEncoding.IncludeList;
import org.uncertweb.ps.encoding.xml.AbstractXMLEncoding.IncludeType;
import org.uncertweb.test.util.TestUtils;


public class PrimitiveEncodingTest {

	private PrimitiveEncoding encoding;

	@Before
	public void before() {
		encoding = new PrimitiveEncoding();
	}
	
	@Test
	public void supportedTypes() {
		Class<?>[] types = { Double.class, Double[].class, Integer.class, Integer[].class, String.class, String[].class,
				Float.class, Float[].class, DateMidnight.class, DateMidnight[].class, URI.class, URI[].class,
				Boolean.class, Boolean[].class };
		for (Class<?> type : types) {
			assertTrue(encoding.isSupportedType(type));
		}
	}
	
	@Test
	public void supportedMimeTypes() {
		assertTrue(encoding.isSupportedMimeType("text/xml"));
		assertTrue(encoding.isSupportedMimeType("text/plain"));
	}
	
	@Test
	public void namespace() {
		assertEquals("http://www.w3.org/2001/XMLSchema", encoding.getNamespace());
	}
	
	@Test
	public void schemaLocation() {
		assertNull(encoding.getSchemaLocation());
	}
	
	@Test
	public void defaultMimeType() {
		assertEquals("text/plain", encoding.getDefaultMimeType());
	}

	@Test
	public void parseString() throws ParseException {
		String actual = parseFromString("one two  three", String.class);
		assertEquals("one two  three", actual);
	}

	@Test
	public void parseStringArray() throws ParseException {
		String[] actual = parseFromString("one two	three", String[].class);
		assertArrayEquals(new String[] {"one", "two", "three" }, actual);
	}

	@Test
	public void parseDouble() throws ParseException {
		Double actual = parseFromString("1.5", Double.class);
		assertEquals(new Double(1.5d), actual);
	}

	@Test
	public void parseDoubleArray() throws ParseException {
		Double[] actual = parseFromString("1.5 6.45 0.0005", Double[].class);
		assertArrayEquals(new Double[] { 1.5, 6.45, 0.0005d }, actual);
	}

	@Test
	public void parseBoolean() throws ParseException {
		Boolean actual = parseFromString("false", Boolean.class);
		assertFalse(actual);
	}

	@Test
	public void parseBooleanArray() throws ParseException {
		Boolean[] actual = parseFromString("false true 0 1", Boolean[].class);
		assertArrayEquals(new Boolean[] { false, true, false, true }, actual);
	}
	
	@Test
	public void includeBoolean() {
		testInclude(Boolean.class, "boolean");
	}
	
	@Test
	public void includeBooleanArray() {
		testInclude(Boolean[].class, "boolean");
	}

	@Test
	public void parseInteger() throws ParseException {
		Integer actual = parseFromString("55", Integer.class);
		assertEquals(new Integer(55), actual);
	}

	@Test
	public void parseIntegerArray() throws ParseException {
		Integer[] actual = parseFromString("1 4 5", Integer[].class);
		assertArrayEquals(new Integer[] { 1, 4, 5 }, actual);
	}

	@Test
	public void parseDate() throws ParseException {
		DateMidnight actual = parseFromString("2012-05-19", DateMidnight.class);
		assertEquals(TestUtils.createDateMidnight(2012, 5, 19, DateTimeZone.getDefault().getID()), actual);
	}

	@Test
	public void parseDateArray() throws ParseException {
		DateMidnight[] actual = parseFromString("2012-12-25 2012-05-19+06:00 2012-05-19-06:00 2013-01-01Z", DateMidnight[].class);
		assertArrayEquals(new DateMidnight[] {
				TestUtils.createDateMidnight(2012, 12, 25, DateTimeZone.getDefault().getID()),
				TestUtils.createDateMidnight(2012, 5, 19, "+06:00"),
				TestUtils.createDateMidnight(2012, 5, 19, "-06:00"),
				TestUtils.createDateMidnight(2013, 1, 1, "UTC")
		}, actual);		
	}
	
	@Test
	public void includeDate() {
		testInclude(DateMidnight.class, "date");
	}
	
	@Test
	public void includeDateArray() {
		testInclude(DateMidnight[].class, "date");
	}

	@Test
	public void parseFloat() throws ParseException {
		Float data = parseFromString("0.1", Float.class);
		assertEquals(new Float(0.1f), data);
	}

	@Test
	public void parseFloatArray() throws ParseException {
		Float[] actual = parseFromString("0.1 0.4 0.6", Float[].class);
		Float[] expected = new Float[] { 0.1f, 0.4f, 0.6f };
		assertArrayEquals(expected, actual);
	}

	@Test
	public void parseURI() throws ParseException {
		URI data = parseFromString("http://www.google.com:80/", URI.class);
		try {
			assertEquals(new URI("http://www.google.com:80/"), data);
		}
		catch (URISyntaxException e) { }
	}

	@Test
	public void parseURIArray() throws ParseException {
		String text = "http://www.google.com:80/ http://www.uncertweb.org";
		URI[] actual = parseFromString(text, URI[].class);
		try {
			assertArrayEquals(new URI[] {
					new URI("http://www.google.com:80/"),
					new URI("http://www.uncertweb.org")
			}, actual);
		}
		catch (URISyntaxException e) { }
	}
	
	@Test
	public void includeURI() {
		testInclude(URI.class, "anyURI");
	}
	
	@Test
	public void includeURIArray() {
		testInclude(URI[].class, "anyURI");
	}

	@Test
	public void encodeString() throws EncodeException {
		Content content = encoding.encode(" h e l l o ");
		testTextContent(content, " h e l l o ");
	}
	
	@Test
	public void encodeStringArray() throws EncodeException {
		Content content = encoding.encode(new String[] { "hey", "ho", "lets", "go" });
		testTextContent(content, "hey ho lets go");
	}

	@Test
	public void encodeDouble() throws EncodeException {
		Content content = encoding.encode(1.1d);
		testTextContent(content, "1.1");
	}

	@Test
	public void encodeBoolean() throws EncodeException {
		Content content = encoding.encode(true);
		testTextContent(content, "true");
	}

	@Test
	public void encodeInteger() throws EncodeException {
		Content content = encoding.encode(1010101);
		testTextContent(content, "1010101");
	}
	
	@Test
	public void encodeIntegerArray() throws EncodeException {
		Content content = encoding.encode(new Integer[] { 1, 2, 3, 4 });
		testTextContent(content, "1 2 3 4");
	}

	@Test
	public void encodeDate() throws EncodeException {
		Content content = encoding.encode(TestUtils.createDateMidnight(2012, 5, 19, "+01:00"));
		testTextContent(content, "2012-05-19+01:00");
	}
	
	@Test
	public void encodeDateArray() throws EncodeException {
		Content content = encoding.encode(new DateMidnight[] {
				TestUtils.createDateMidnight(2012, 5, 19, "+06:00"),
				TestUtils.createDateMidnight(2012, 5, 19, "-06:00")
		});
		testTextContent(content, "2012-05-19+06:00 2012-05-19-06:00");
	}

	@Test
	public void encodeFloat() throws EncodeException {
		Content content = encoding.encode(11.1f);
		testTextContent(content, "11.1");
	}

	@Test
	public void encodeURI() throws EncodeException {
		try {
			Content content = encoding.encode(new URI("http://www.google.com:80/"));
			testTextContent(content, "http://www.google.com:80/");
		}
		catch (URISyntaxException e) { }
	}	
	
	@Test
	public void encodeURIArray() throws EncodeException {
		try {
			Content content = encoding.encode(new URI[] {
					new URI("http://www.google.com:80/"),
					new URI("http://www.uncertweb.org")
			});
			testTextContent(content, "http://www.google.com:80/ http://www.uncertweb.org");
		}
		catch (URISyntaxException e) { }
	}

	private void testTextContent(Content content, String expected) {
		assertTrue(content instanceof Text);
		Text text = (Text)content;
		assertEquals(expected, text.getText());
	}
	
	private void testInclude(Class<?> type, String expected) {
		Include include = encoding.getInclude(type);
		if (type.isArray()) {
			assertTrue(include instanceof IncludeList);
		}
		else {
			assertTrue(include instanceof IncludeType);
		}
		assertEquals(expected, include.getName());
	}

	private <T> T parseFromString(String text, Class<T> target) throws ParseException {
		Content content = new Text(text);
		return encoding.parse(content, target);
	}

}
