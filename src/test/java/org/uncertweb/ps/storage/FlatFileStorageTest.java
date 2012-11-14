package org.uncertweb.ps.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class FlatFileStorageTest {
	
	private static final String TEST_STRING = "i am a string in a file!";
	
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
	public void putReturnsId() throws StorageException, IOException {
		String id = putTestString();
		assertThat(id, notNullValue());
	}
	
	@Test
	public void putCreatesFile() throws StorageException, IOException {
		String id = putTestString();
		Path stored = base.resolve(id);
		assertThat(Files.isRegularFile(stored), equalTo(true));
	}
	
	@Test
	public void putCreatesNonEmptyFile() throws StorageException, IOException {
		String id = putTestString();
		Path stored = base.resolve(id);
		assertThat(Files.size(stored), greaterThan(0l));
	}
	
	@Test
	public void putCreatesEntryFile() throws StorageException {
		String id = putTestString();
		Path entry = base.resolve(id + ".entry");
		assertThat(Files.isRegularFile(entry), equalTo(true));
	}
	
	@Test
	public void putCreatesNonEmptyEntryFile() throws StorageException, IOException {
		String id = putTestString();
		Path entry = base.resolve(id + ".entry");
		assertThat(Files.size(entry), greaterThan(0l));
	}
	
	@Test
	public void getReturnsEntry() throws StorageException {
		StorageEntry entry = putAndGetTestString();
		assertThat(entry, notNullValue());
	}
	
	@Test
	public void getReturnsEntryWithContent() throws StorageException {
		StorageEntry entry = putAndGetTestString();
		byte[] content = TEST_STRING.getBytes();
		assertThat(entry.getContent(), equalTo(content));
	}
		
	@Test
	public void getReturnsEntryWithMimeType() throws StorageException {
		StorageEntry entry = putAndGetTestString();
		assertThat(entry.getMimeType(), equalTo("text/plain"));
	}
	
	@Test
	public void getReturnsEntryWithStoredBy() throws StorageException {
		StorageEntry entry = putAndGetTestString();
		assertThat(entry.getStoredBy(), equalTo("some_user"));
	}
	
	@Test
	public void getReturnsEntryWithStoredAt() throws StorageException {
		StorageEntry entry = putAndGetTestString();
		assertThat(entry.getStoredAt(), notNullValue());
	}
	
	@Test
	public void removeRemovesFile() throws StorageException {
		String id = putTestString();
		storage.remove(id);
		Path stored = base.resolve(id);
		assertThat(Files.exists(stored), equalTo(false));
	}
	
	@Test
	public void removeRemovesEntryFile() throws StorageException {
		String id = putTestString();
		storage.remove(id);
		Path entry = base.resolve(id + ".entry");
		assertThat(Files.exists(entry), equalTo(false));
	}
	
	@Test
	public void putReturnsUniqueIds() throws StorageException {
		String firstId = putTestString();
		String secondId = putTestString();
		assertThat(firstId, not(equalTo(secondId)));
	}
	
	private String putTestString() throws StorageException {
		return storage.put(TEST_STRING.getBytes(), "text/plain", "some_user");
	}
	
	private StorageEntry putAndGetTestString() throws StorageException {
		String id = storage.put(TEST_STRING.getBytes(), "text/plain", "some_user");
		return storage.get(id);
	}

}
