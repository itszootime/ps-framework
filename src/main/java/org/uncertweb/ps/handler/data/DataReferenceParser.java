package org.uncertweb.ps.handler.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.uncertweb.ps.encoding.Encoding;
import org.uncertweb.ps.encoding.EncodingRepository;
import org.uncertweb.ps.encoding.ParseException;
import org.uncertweb.util.Stopwatch;

public class DataReferenceParser {

	private static final Logger logger = Logger.getLogger(DataReferenceParser.class);

	public <T> T parse(URL url, Class<T> type) throws ParseException {
		return this.parse(url, type, false);
	}

	public <T> T parse(URL url, Class<T> type, boolean compressed) throws ParseException {
		// get repo and encodings
		EncodingRepository encodingRepo = EncodingRepository.getInstance();
		Encoding[] encodings = new Encoding[] {
				encodingRepo.getBinaryEncoding(type),
				encodingRepo.getXMLEncoding(type),
				encodingRepo.getJSONEncoding(type)
		};

		// get bytes
		byte[] bytes;
		try (InputStream inputStream = url.openStream()) {
			bytes = IOUtils.toByteArray(inputStream);
		}
		catch (IOException e) {
			throw new ParseException("Couldn't read data from " + url.toString(), e);
		}

		// try parse
		for (Encoding encoding : encodings) {
			if (encoding != null) {
				logger.debug("Trying to parse " + type.getSimpleName() + " with " + encoding.getClass().getSimpleName() + " class.");

				try (InputStream byteStream = new ByteArrayInputStream(bytes)) {
					try {
						return parseFromStream(byteStream, encoding, type, compressed);
					}
					catch (ParseException e) {
						// we are trying multiple encoding classes, this may happen
					}
				}
				catch (IOException e) {
					// not a problem with multiple encoding classes
				}
			}
		}

		// we got nothing
		throw new ParseException("Couldn't parse " + type.getSimpleName() + " with any encoding class.");
	}

	public <T> T parse(URL url, Class<T> type, String mimeType) throws ParseException {
		return this.parse(url, type, mimeType, false);
	}

	public <T> T parse(URL url, Class<T> type, String mimeType, boolean compressed) throws ParseException {
		// get repo and encoding
		EncodingRepository encodingRepo = EncodingRepository.getInstance();
		Encoding encoding = encodingRepo.getEncoding(type, mimeType);

		// check if encoding exists
		if (encoding == null) {
			throw new ParseException("No suitable encoding found for MIME type " + mimeType + ".");
		}
		else {
			logger.debug("Parsing " + mimeType + " with " + encoding.getClass().getSimpleName() + ".");

			try (InputStream inputStream = url.openStream()) {
				return parseFromStream(inputStream, encoding, type, compressed);
			}
			catch (IOException e) {
				throw new ParseException("Couldn't read data from " + url.toString(), e);
			}
		}
	}

	private <T> T parseFromStream(InputStream inputStream, Encoding encoding, Class<T> type, boolean compressed) throws ParseException {
		// check if there's any compression
		InputStream finalStream = inputStream;
		if (compressed) {
			finalStream = createZipStream(inputStream);
		}

		// parse
		Stopwatch timer = new Stopwatch();				
		T object = encoding.parse(finalStream, type);
		logger.debug("Took " + timer.getElapsedTime() + " to parse referenced data.");

		return object;
	}

	private ZipInputStream createZipStream(InputStream inputStream) throws ParseException {
		try {
			ZipInputStream zipStream = new ZipInputStream(inputStream);
			ZipEntry e = zipStream.getNextEntry();
			if (e == null) {
				throw new ParseException("No files found in zip!");
			}
			logger.debug("Compressed file referenced, will parse first entry: " + e.getName() + ".");
			return zipStream;
		}
		catch (IOException e) {
			throw new ParseException("Couldn't read compressed data.", e);
		}
	}

}
