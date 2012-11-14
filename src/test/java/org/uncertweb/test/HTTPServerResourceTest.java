package org.uncertweb.test;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;

public class HTTPServerResourceTest {

	@Rule
	public HTTPServerResource server = new HTTPServerResource(8000);

	@Test
	public void httpServerWithString() throws IOException {
		// load from file
		String fileContent = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("xml/polygon.xml"));
		
		// load from server
		server.addFileHandler("xml/polygon.xml");
		URL url = new URL("http://localhost:8000/xml/polygon.xml");
		URLConnection conn = url.openConnection();
		String serverContent = IOUtils.toString(conn.getInputStream());
		
		// check
		Assert.assertEquals(fileContent, serverContent);
	}
	
	@Test
	public void httpServerWithBinary() throws IOException {
		// load from file
		byte[] fileContent = IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream("xml/polygon.zip"));
		
		// load from server
		server.addFileHandler("xml/polygon.zip");
		URL url = new URL("http://localhost:8000/xml/polygon.zip");
		URLConnection conn = url.openConnection();
		byte[] serverContent = IOUtils.toByteArray(conn.getInputStream());
		
		// check
		SupAssert.assertArrayEquals(fileContent, serverContent);
	}
	
}
