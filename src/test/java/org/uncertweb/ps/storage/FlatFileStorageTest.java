package org.uncertweb.ps.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.uncertweb.test.SupAssert;

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
		Assert.assertNotNull(id);
		
		// check file
		Path stored = base.resolve(id);
		Assert.assertTrue(Files.isRegularFile(stored));
		Assert.assertTrue(Files.size(stored) > 0);
		Path entry = base.resolve(id + ".entry");
		Assert.assertTrue(Files.isRegularFile(entry));
		Assert.assertTrue(Files.size(entry) > 0);
	}
	
	@Test
	public void get() throws StorageException {
		// put first
		byte[] content = "i am a string in a file!".getBytes();
		String id = storage.put(content, "text/plain", "some_user");
		
		// now get
		StorageEntry entry = storage.get(id);
		Assert.assertNotNull(entry);
		SupAssert.assertArrayEquals(content, entry.getContent());
		Assert.assertEquals("text/plain", entry.getMimeType());
		Assert.assertEquals("some_user", entry.getStoredBy());
		Assert.assertNotNull(entry.getStoredAt());
	}
	
	@Test
	public void remove() throws StorageException {
		// put
		String id = storage.put("i am a string in a file!".getBytes(), "text/plain", "some_user");
		
		// remove
		boolean removed = storage.remove(id);
		
		// check file
		Assert.assertTrue(removed);
		Path stored = base.resolve(id);
		Assert.assertFalse(Files.exists(stored));
		Path entry = base.resolve(id + ".entry");
		Assert.assertFalse(Files.exists(entry));
	}
	
	@Test
	public void ids() throws StorageException {
		String id1 = storage.put("i am a string in a file!".getBytes(), "text/plain", "some_user");
		String id2 = storage.put("i am a string in a file!".getBytes(), "text/plain", "another_user");
		Assert.assertNotSame(id1, id2);
	}

}
