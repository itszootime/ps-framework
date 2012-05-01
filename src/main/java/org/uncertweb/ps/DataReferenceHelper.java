package org.uncertweb.ps;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.uncertweb.ps.data.DataDescription;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.Encoding;
import org.uncertweb.ps.encoding.EncodingRepository;
import org.uncertweb.ps.encoding.ParseException;

public class DataReferenceHelper {
	
	private static final Logger logger = Logger.getLogger(DataReferenceHelper.class);

	public static Object parseDataReference(String href, String mimeType, boolean compressed, DataDescription dataDescription) throws IOException, ParseException {
		// FIXME: referenced xml will not be validated
		// FIXME: referenced simpledata
		URL dataURL = new URL(href);
		if (!dataDescription.isRaw()) {
			InputStream inputStream = dataURL.openStream();
			try {			
				// check if there's any compression
				if (compressed) {
					inputStream = new ZipInputStream(inputStream);
					ZipEntry e = ((ZipInputStream) inputStream).getNextEntry(); // only read first entry
					logger.info("Compressed file referenced, will parse first entry: '" + e.getName() + "'.");
				}

				// get encoding and parse
				Stopwatch timer = new Stopwatch();
				Encoding encoding = EncodingRepository.getInstance().getEncoding(dataDescription.getClassOf(), mimeType);
				logger.debug("Parsing referenced data using " + encoding.getClass().getSimpleName() + "...");
				Object data = encoding.parse(new BufferedInputStream(inputStream), dataDescription.getClassOf());
				logger.debug("Parsed referenced data in " + timer.getElapsedTime() + ".");
				
				return data;
			}
			finally {
				inputStream.close();
			}
		}
		else {
			return dataURL;
		}
	}

	public static URL generateDataReference(Object data, DataDescription dataDescription, String basePath, String baseURL) throws IOException, EncodeException {
		URL dataURL;
		if (dataDescription.isRaw()) {
			dataURL = (URL) data; 
		}
		else {
			// FIXME: not secure, no storage of mime type, this method isn't reliable either
			// get path to save
			String id = String.valueOf(System.currentTimeMillis());
			String filename = "out_" + id;
			File dataDir = new File(basePath + System.getProperty("file.separator") + "WEB-INF" + System.getProperty("file.separator")); // + "data");
			System.out.println(dataDir.toString());
			if (!dataDir.isDirectory()) {
				dataDir.mkdir();
			}

			// generate to file
			// FIXME: what about xml simple types
			FileOutputStream fos = new FileOutputStream(new File(dataDir, filename));
			EncodingRepository.getInstance().getEncoding(dataDescription.getClassOf()).encode(data, fos);
			fos.close();

			// set url
			dataURL = new URL(baseURL + "/data?id=" + id);
		}
		return dataURL;		
	}

}
