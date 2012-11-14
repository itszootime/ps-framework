package org.uncertweb.ps.test;

import java.io.IOException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.uncertweb.ps.process.ProcessRepository;
import org.uncertweb.ps.test.process.BufferPolygonProcess;
import org.uncertweb.ps.test.process.HashProcess;
import org.uncertweb.ps.test.process.SumProcess;

public class Utilities {
	
	/**
	 * Initialises repository classes for testing. This ensures we are not 
	 * relying on the config loading from file.
	 * 
	 */
	public static void setupProcessRepository() {
		// add processes
		ProcessRepository repo = ProcessRepository.getInstance();
		repo.addProcess(new HashProcess());
		repo.addProcess(new SumProcess());
		repo.addProcess(new BufferPolygonProcess());
	}
	
	public static Document loadXML(String filename) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		builder.setIgnoringBoundaryWhitespace(true);
		return builder.build(Utilities.class.getClassLoader().getResourceAsStream("xml/" + filename));
	}	
	
	public static DateMidnight createDateMidnight(int year, int month, int day, String timezone) {
		return new DateMidnight(new DateTime(year, month, day, 0, 0, 0, 0, DateTimeZone.forID(timezone)));
	}

}
