package org.uncertweb.ps.handler.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.uncertweb.ps.data.DataReference;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.storage.Storage;
import org.uncertweb.ps.storage.StorageException;
import org.uncertweb.ps.test.ConfiguredService;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class DataReferenceGeneratorTest {
	
	@Rule
	public ConfiguredService service = new ConfiguredService();
	
	private DataReferenceGenerator generator;
	
	@Before
	public void before() throws IOException {
		generator = new DataReferenceGenerator();
	}
	
	@Test
	public void generateReturnsSensibleURL() throws EncodeException, StorageException {
		DataReference ref = generateTestDataReference();
		assertThat(ref.getURL().toString(), startsWith(service.getBaseURL() + "/data/"));
	}
	
	@Test
	public void generateReturnsMimeType() throws EncodeException, StorageException {
		DataReference ref = generateTestDataReference();
		assertThat(ref.getMimeType(), equalTo("text/xml"));
	}
	
	@Test
	public void generateReturnsCompressed() throws EncodeException, StorageException {
		DataReference ref = generateTestDataReference();
		assertThat(ref.isCompressed(), equalTo(false));
	}
	
	@Test
	public void generateStoresData() throws EncodeException, StorageException {
		String url = generateTestDataReference().getURL().toString();
		String id = url.substring(url.lastIndexOf("/") + 1);
		assertThat(Storage.getInstance().get(id), notNullValue());
	}
	
	private DataReference generateTestDataReference() throws EncodeException, StorageException {
		Point point = new GeometryFactory().createPoint(new Coordinate(-2.63, 51.16));
		return generator.generate(point);
	}

}
