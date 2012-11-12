package org.uncertweb.ps.encoding.xml;

import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.Assert;

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
import org.uncertweb.ps.test.Utilities;
import org.uncertweb.test.SupAssert;

public class PrimitiveEncodingTest {

	private PrimitiveEncoding encoding;

	@Before
	public void setUp() {
		encoding = new PrimitiveEncoding();
	}
	
	@Test
	public void supportedTypes() {
		Class<?>[] types = { Double.class, Double[].class, Integer.class, Integer[].class, String.class, String[].class,
				Float.class, Float[].class, DateMidnight.class, DateMidnight[].class, URI.class, URI[].class,
				Boolean.class, Boolean[].class };
		for (Class<?> type : types) {
			Assert.assertTrue(encoding.isSupportedType(type));
		}
	}
	
	@Test
	public void supportedMimeType() {
		Assert.assertTrue(encoding.isSupportedMimeType("text/xml"));
	}
	
	@Test
	public void namespace() {
		Assert.assertEquals("http://www.w3.org/2001/XMLSchema", encoding.getNamespace());
	}
	
	@Test
	public void schemaLocation() {
		Assert.assertNull(encoding.getSchemaLocation());
	}

	@Test
	public void parseString() throws ParseException {
		String actual = parseFromString("one two  three", String.class);
		Assert.assertEquals("one two  three", actual);
	}

	@Test
	public void parseStringArray() throws ParseException {
		String[] actual = parseFromString("one two	three", String[].class);
		SupAssert.assertArrayEquals(new String[] {"one", "two", "three" }, actual);
	}

	@Test
	public void parseDouble() throws ParseException {
		Double actual = parseFromString("1.5", Double.class);
		Assert.assertEquals(1.5d, actual);
	}

	@Test
	public void parseDoubleArray() throws ParseException {
		Double[] actual = parseFromString("1.5 6.45 0.0005", Double[].class);
		SupAssert.assertArrayEquals(new Double[] { 1.5, 6.45, 0.0005d }, actual);
	}

	@Test
	public void parseBoolean() throws ParseException {
		Boolean actual = parseFromString("false", Boolean.class);
		Assert.assertFalse(actual);
	}

	@Test
	public void parseBooleanArray() throws ParseException {
		Boolean[] actual = parseFromString("false true 0 1", Boolean[].class);
		SupAssert.assertArrayEquals(new Boolean[] { false, true, false, true }, actual);
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
		Assert.assertEquals(new Integer(55), actual);
	}

	@Test
	public void parseIntegerArray() throws ParseException {
		Integer[] actual = parseFromString("1 4 5", Integer[].class);
		SupAssert.assertArrayEquals(new Integer[] { 1, 4, 5 }, actual);
	}

	@Test
	public void parseDate() throws ParseException {
		DateMidnight actual = parseFromString("2012-05-19", DateMidnight.class);
		Assert.assertEquals(Utilities.createDateMidnight(2012, 5, 19, DateTimeZone.getDefault().getID()), actual);
	}

	@Test
	public void parseDateArray() throws ParseException {
		DateMidnight[] actual = parseFromString("2012-12-25 2012-05-19+06:00 2012-05-19-06:00 2013-01-01Z", DateMidnight[].class);
		SupAssert.assertArrayEquals(new DateMidnight[] {
				Utilities.createDateMidnight(2012, 12, 25, DateTimeZone.getDefault().getID()),
				Utilities.createDateMidnight(2012, 5, 19, "+06:00"),
				Utilities.createDateMidnight(2012, 5, 19, "-06:00"),
				Utilities.createDateMidnight(2013, 1, 1, "UTC")
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
		Assert.assertEquals(0.1f, data);
	}

	@Test
	public void parseFloatArray() throws ParseException {
		Float[] actual = parseFromString("0.1 0.4 0.6", Float[].class);
		SupAssert.assertArrayEquals(new Float[] { 0.1f, 0.4f, 0.6f }, actual);
	}

	@Test
	public void parseURI() throws ParseException {
		URI data = parseFromString("http://www.google.com:80/", URI.class);
		try {
			Assert.assertEquals(new URI("http://www.google.com:80/"), data);
		}
		catch (URISyntaxException e) { }
	}

	@Test
	public void parseURIArray() throws ParseException {
		String text = "http://www.google.com:80/ http://www.uncertweb.org";
		URI[] actual = parseFromString(text, URI[].class);
		try {
			SupAssert.assertArrayEquals(new URI[] {
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
		Content content = encoding.encode(Utilities.createDateMidnight(2012, 5, 19, "+01:00"));
		testTextContent(content, "2012-05-19+01:00");
	}
	
	@Test
	public void encodeDateArray() throws EncodeException {
		Content content = encoding.encode(new DateMidnight[] {
				Utilities.createDateMidnight(2012, 5, 19, "+06:00"),
				Utilities.createDateMidnight(2012, 5, 19, "-06:00")
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
		Assert.assertTrue(content instanceof Text);
		Text text = (Text)content;
		Assert.assertEquals(expected, text.getText());
	}
	
	private void testInclude(Class<?> type, String expected) {
		Include include = encoding.getInclude(type);
		if (type.isArray()) {
			Assert.assertTrue(include instanceof IncludeList);
		}
		else {
			Assert.assertTrue(include instanceof IncludeType);
		}
		Assert.assertEquals(expected, include.getName());
	}

	private <T> T parseFromString(String text, Class<T> target) throws ParseException {
		Content content = new Text(text);
		return encoding.parse(content, target);
	}

}
