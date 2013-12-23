package org.uncertweb.ps.handler.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

import org.uncertweb.ps.Config;
import org.uncertweb.ps.data.DataReference;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.Encoding;
import org.uncertweb.ps.encoding.EncodingRepository;
import org.uncertweb.ps.storage.Storage;
import org.uncertweb.ps.storage.StorageException;

public class DataReferenceGenerator {
	
	// FIXME: need a priority flag! (e.g. XML, JSON)
	public <T> DataReference generate(T object, final Class<? extends Encoding> priority) throws EncodeException, StorageException {
		// find encoding
		Class<?> type = object.getClass();
		EncodingRepository repo = EncodingRepository.getInstance();
		Encoding[] encodings = new Encoding[] {
				repo.getBinaryEncoding(type),
				repo.getXMLEncoding(type),
				repo.getJSONEncoding(type)
		};
		
		// respect priority
		Arrays.sort(encodings, new Comparator<Encoding>() {
			@Override
			public int compare(Encoding e1, Encoding e2) {                                                                                            
				boolean is1 = priority.isInstance(e1);                                                                                                  
				boolean is2 = priority.isInstance(e2);
				if (is1 && !is2) {
					return -1;
				} else if (is2 && !is1) {
					return 1;
				}
				return 0;
			}
		});
		
		// encode
		for (Encoding encoding : encodings) {
			if (encoding != null) {
				try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
					// encode
					encoding.encode(object, byteStream);
					String mimeType = encoding.getDefaultMimeType();
					byte[] content = byteStream.toByteArray();
				
					// store
					Storage storage = Storage.getInstance();
					String id = storage.put(content, mimeType, "ps-framework");
					
					// generate url
					String baseURL = Config.getInstance().getServerProperty("baseURL") + "/data/";
					return new DataReference(new URL(baseURL + id), mimeType);
				}
				catch (IOException e) {
					throw new EncodeException("Couldn't write data to stream.", e);
				}
			}
		}
		
		// couldn't find encoding
		throw new EncodeException("No suitable encoding found for " + type.getSimpleName() + ".");
	}
	
}
