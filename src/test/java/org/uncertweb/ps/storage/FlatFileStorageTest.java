package org.uncertweb.ps.storage;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class FlatFileStorageTest {
	
	@Rule
	public TemporaryFolder storageFolder = new TemporaryFolder();
	
	@Rule
    public ExpectedException exception = ExpectedException.none();
	
	private Path base;
	private Storage storage;
	
	@Before
	public void before() throws IOException {
		base = storageFolder.newFolder().toPath();
		storage = new FlatFileStorage(base);
	}

	@Test
	public void put() throws StorageException, IOException {
		// put
		String id = storage.put("i am a string in a file!".getBytes(), "text/plain", "some_user");
		assertNotNull(id);
		
		// check file
		Path stored = base.resolve(id);
		assertTrue(Files.isRegularFile(stored));
		assertTrue(Files.size(stored) > 0);
		Path entry = base.resolve(id + ".entry");
		assertTrue(Files.isRegularFile(entry));
		assertTrue(Files.size(entry) > 0);
	}
	
	@Test
	public void get() throws StorageException {
		// put first
		byte[] content = "i am a string in a file!".getBytes();
		String id = storage.put(content, "text/plain", "some_user");
		
		// now get
		StorageEntry entry = storage.get(id);
		assertNotNull(entry);
		assertArrayEquals(content, entry.getContent());
		assertEquals("text/plain", entry.getMimeType());
		assertEquals("some_user", entry.getStoredBy());
		assertNotNull(entry.getStoredAt());
	}
	
	@Test
	public void remove() throws StorageException {
		// put
		String id = storage.put("i am a string in a file!".getBytes(), "text/plain", "some_user");
		
		// remove
		boolean removed = storage.remove(id);
		
		// check file
		assertTrue(removed);
		Path stored = base.resolve(id);
		assertFalse(Files.exists(stored));
		Path entry = base.resolve(id + ".entry");
		assertFalse(Files.exists(entry));
	}
	
	@Test
	public void ids() throws StorageException {
		String id1 = storage.put("i am a string in a file!".getBytes(), "text/plain", "some_user");
		String id2 = storage.put("i am a string in a file!".getBytes(), "text/plain", "another_user");
		assertNotSame(id1, id2);
	}

}
