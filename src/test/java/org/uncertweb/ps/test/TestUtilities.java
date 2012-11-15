package org.uncertweb.ps.test;

import java.io.IOException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class TestUtilities {
	
	public static Document loadXML(String filename) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		builder.setIgnoringBoundaryWhitespace(true);
		return builder.build(TestUtilities.class.getClassLoader().getResourceAsStream("xml/" + filename));
	}	
	
	public static DateMidnight createDateMidnight(int year, int month, int day, String timezone) {
		return new DateMidnight(new DateTime(year, month, day, 0, 0, 0, 0, DateTimeZone.forID(timezone)));
	}

}
