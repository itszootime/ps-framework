package org.uncertweb.ps.encoding;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.uncertweb.ps.Config;
import org.uncertweb.ps.encoding.binary.AbstractBinaryEncoding;
import org.uncertweb.ps.encoding.xml.AbstractXMLEncoding;
import org.uncertweb.ps.encoding.xml.GMLEncoding;
import org.uncertweb.ps.encoding.xml.OMEncoding;
import org.uncertweb.ps.encoding.xml.UncertMLEncoding;


public class EncodingRepository {

	private List<Encoding> encodings;
	private static EncodingRepository instance;
	private static final Logger logger = Logger.getLogger(EncodingRepository.class);

	private EncodingRepository() {
		encodings = new ArrayList<Encoding>();
		
		// add built-in encodings
		encodings.add(new GMLEncoding());
		encodings.add(new OMEncoding());
		encodings.add(new UncertMLEncoding());
		
		// add from user config
		// inserting at index 0 means they will be preferred over built-in encodings
		List<String> encodingClasses = Config.getInstance().getEncodingClasses();
		for (String c : encodingClasses) {
			try {
				encodings.add(0, (Encoding) Class.forName(c).newInstance());
				logger.info("Loaded encoding class " + c + ".");
			}
			catch (ClassNotFoundException e) {
				logger.error("Couldn't find encoding class " + c + ", skipping.");
			}
			catch (InstantiationException e) {
				logger.error("Couldn't instantiate encoding class " + c + ", skipping.");
			}
			catch (IllegalAccessException e) {
				logger.error("Couldn't access encoding class " + c + ", skipping.");
			}
			catch (ClassCastException e) {
				logger.error("Encoding class " + c + " does not implement org.uncertweb.ps.encoding.Encoding, skipping.");
			}
		}
	}

	public static EncodingRepository getInstance() {
		if (instance == null) {
			instance = new EncodingRepository();
		}
		return instance;
	}

	// this'll give you the first supported encoding it encounters
	public Encoding getEncoding(Class<?> classOf) {
		for (Encoding encoding : encodings) {
			if (encoding.isSupportedClass(classOf)) {
				return encoding;
			}
		}
		return null;
	}
	
	// this'll give you the first supported encoding it encounters
	public AbstractXMLEncoding getXMLEncoding(Class<?> classOf) {
		for (Encoding encoding : encodings) {
			if (encoding.isSupportedClass(classOf) && encoding instanceof AbstractXMLEncoding) {
				return (AbstractXMLEncoding) encoding;
			}
		}
		return null;
	}
	
	public AbstractBinaryEncoding getBinaryEncoding(Class<?> classOf) {
		for (Encoding encoding : encodings) {
			if (encoding.isSupportedClass(classOf) && encoding instanceof AbstractBinaryEncoding) {
				return (AbstractBinaryEncoding) encoding;
			}
		}
		return null;
	}
	
	// this'll give you the first supported encoding it encounters
	public Encoding getEncoding(Class<?> classOf, String mimeType) {
		for (Encoding encoding : encodings) {
			if (encoding.isSupportedClass(classOf) && encoding.isSupportedMimeType(mimeType)) {
				return encoding;
			}
		}
		return null;
	}

}
