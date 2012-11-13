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
	public void httpServer() throws IOException {
		// load from file
		String fileContent = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("sum-request.xml"));
		
		// load from server
		server.addFileHandler("sum-request.xml");
		URL url = new URL("http://localhost:8000/sum-request.xml");
		URLConnection conn = url.openConnection();
		String serverContent = IOUtils.toString(conn.getInputStream());
		
		// check
		Assert.assertEquals(fileContent, serverContent);
	}
	
}
