package org.uncertweb.ps.handler.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.uncertweb.ps.encoding.Encoding;
import org.uncertweb.ps.encoding.EncodingRepository;
import org.uncertweb.ps.encoding.ParseException;
import org.uncertweb.util.Stopwatch;

public class DataReferenceParser {

	private static final Logger logger = Logger.getLogger(DataReferenceParser.class);

	public <T> T parse(URL url, Class<T> type, String mimeType) throws ParseException {
		return this.parse(url, type, mimeType, false);
	}

	public <T> T parse(URL url, Class<T> type, String mimeType, boolean compressed) throws ParseException {
		// get repo and encoding
		EncodingRepository encodingRepo = EncodingRepository.getInstance();
		Encoding encoding = encodingRepo.getEncoding(type, mimeType);

		// check if encoding exists
		if (encoding == null) {
			throw new ParseException("Unsupported MIME type " + mimeType + ".");
		}
		else {
			logger.debug("Using " + encoding.getClass().getSimpleName() + " encoding class for " + mimeType + "...");
			
			try (InputStream inputStream = url.openStream()) {
				// check if there's any compression
				InputStream finalStream = inputStream;
				if (compressed) {
					finalStream = createZipStream(inputStream);
				}

				// parse
				Stopwatch timer = new Stopwatch();				
				T object = encoding.parse(finalStream, type);
				logger.info("Took " + timer.getElapsedTime() + " to parse referenced data.");

				return object;
			}
			catch (IOException e) {
				throw new ParseException("Couldn't read data from " + url.toString(), e);
			}
		}
	}

	private ZipInputStream createZipStream(InputStream inputStream) throws ParseException {
		try {
			ZipInputStream zipStream = new ZipInputStream(inputStream);
			ZipEntry e = zipStream.getNextEntry();
			if (e == null) {
				throw new ParseException("No files found in zip!");
			}
			logger.info("Compressed file referenced, will parse first entry: " + e.getName() + ".");
			return zipStream;
		}
		catch (IOException e) {
			throw new ParseException("Couldn't read compressed data.", e);
		}
	}

}
