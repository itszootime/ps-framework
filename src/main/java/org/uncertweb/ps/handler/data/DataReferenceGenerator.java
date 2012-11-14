package org.uncertweb.ps.handler.data;

import java.net.URL;

public class DataReferenceGenerator {
	
	public <T> URL generate(T object, boolean compressed) {
		return null;
	}

//	public static URL generateXMLDataReference(Object data, DataDescription dataDescription, String basePath, String baseURL) throws IOException, EncodeException {
//		URL dataURL;
//		if (dataDescription.isRaw()) {
//			dataURL = (URL) data; 
//		}
//		else {
//			// FIXME: not secure, no storage of mime type, this method isn't reliable either
//			// get path to save
//			String id = String.valueOf(System.currentTimeMillis());
//			String filename = "out_" + id;
//			File dataDir = new File(basePath + System.getProperty("file.separator") + "WEB-INF" + System.getProperty("file.separator")); // + "data");
//			System.out.println(dataDir.toString());
//			if (!dataDir.isDirectory()) {
//				dataDir.mkdir();
//			}
//
//			// generate to file
//			// FIXME: what about xml simple types
//
//			// find encoding
//			EncodingRepository repository = EncodingRepository.getInstance();
//			Encoding encoding = repository.getXMLEncoding(dataDescription.getType());
//			if (encoding == null) {
//				encoding = repository.getBinaryEncoding(dataDescription.getType());
//			}
//
//			// if we've got encoding, go
//			if (encoding != null) {
//				FileOutputStream fos = new FileOutputStream(new File(dataDir, filename));
//				encoding.encode(data, fos);
//				fos.close();
//			}
//			else {
//				throw new EncodeException("Couldn't find encoding for " + dataDescription.getType().getSimpleName());
//			}			
//
//			// set url
//			dataURL = new URL(baseURL + "/data?id=" + id);
//		}
//		return dataURL;		
//	}
//
//	public static URL generateJSONDataReference(JsonElement element, String basePath, String baseURL) throws IOException, EncodeException {
//		URL dataURL;
//
//		// FIXME: hacky, skipping isRaw
//
//		// FIXME: not secure, no storage of mime type, this method isn't reliable either
//		// get path to save
//		String id = String.valueOf(System.currentTimeMillis());
//		String filename = "out_" + id;
//		File dataDir = new File(basePath + System.getProperty("file.separator") + "WEB-INF" + System.getProperty("file.separator")); // + "data");
//		System.out.println(dataDir.toString());
//		if (!dataDir.isDirectory()) {
//			dataDir.mkdir();
//		}
//
//		// generate to file			
//		// find encoding
//		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File(dataDir, filename)));
//		new GsonBuilder().create().toJson(element, osw);
//		osw.close();
//
//		// FIXME: need binary too
//
//
//		// set url
//		dataURL = new URL(baseURL + "/data?id=" + id);
//		return dataURL;		
//	}
	
}
