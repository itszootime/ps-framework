package org.uncertweb.ps.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FlatFileStorage extends Storage {

	private Path base;

	public FlatFileStorage(Path base) {
		super();
		this.base = base;
	}

	public String put(byte[] content, String mimeType, String storedBy) throws StorageException {
		// generate id
		String id = UUID.randomUUID().toString();

		// write content
		try {
			Files.write(base.resolve(id), content);
		}
		catch (IOException e) {
			throw new StorageException("Couldn't write content to file.", e);
		}		

		// generate entry json
		String md5 = DigestUtils.md5Hex(content);
		String json = "{\"mimeType\":\"" + mimeType + "\"," +
				"\"storedBy\":\"" + storedBy + "\"," +
				"\"storedAt\":\"" + ISODateTimeFormat.dateTime().print(new DateTime()) + "\"," +
				"\"checksum\":\"" + md5 + "\"}";

		// write entry
		try {
			Files.write(base.resolve(id + ".entry"), json.getBytes());
		}
		catch (IOException e) {
			throw new StorageException("Couldn't write storage entry file.", e);
		}		

		return id;
	}

	public StorageEntry get(String id) throws StorageException {
		if (id == null) {
			throw new IllegalArgumentException("ID cannot be null.");
		}

		Path contentPath = base.resolve(id);
		Path entryPath = base.resolve(id + ".entry");

		// get entry
		String mimeType;
		String storedBy;
		DateTime storedAt;
		String md5;
		try {
			byte[] entryBytes = Files.readAllBytes(entryPath);
			JsonObject entryObject = new JsonParser().parse(new String(entryBytes)).getAsJsonObject();
			mimeType = entryObject.get("mimeType").getAsString();
			storedBy = entryObject.get("storedBy").getAsString();
			storedAt = ISODateTimeFormat.dateTime().parseDateTime(entryObject.get("storedAt").getAsString());
			md5 = entryObject.get("checksum").getAsString();
		}
		catch (IOException e) {
			throw new StorageException("Couldn't read storage entry file, is the ID valid?", e);
		}

		// get content
		byte[] contentBytes;
		try {
			contentBytes = Files.readAllBytes(contentPath);
		}
		catch (IOException e) {
			throw new StorageException("Couldn't read content from file.", e);
		}
		
		// verify
		String newMD5 = DigestUtils.md5Hex(contentBytes);
		if (!md5.equals(newMD5)) {
			throw new StorageException("File failed checksum verification.");
		}
		
		return new StorageEntry(contentBytes, mimeType, storedBy, storedAt);
	}

	public boolean remove(String id) throws StorageException {
		if (id == null) {
			throw new IllegalArgumentException("ID cannot be null.");
		}

		// remove files
		Path contentPath = base.resolve(id);
		Path entryPath = base.resolve(id + ".entry");
		try {			
			Files.delete(contentPath);			
			Files.delete(entryPath);
		}
		catch (IOException e) {
			throw new StorageException("Couldn't remove files.", e);
		}

		return !Files.exists(contentPath) && !Files.exists(entryPath);
	}

}
