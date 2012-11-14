package org.uncertweb.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;

public class HTTPServerTest {

	@Rule
	public HTTPServer server = new HTTPServer(8000);

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
		assertThat(serverContent, equalTo(fileContent));
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
		assertThat(serverContent, equalTo(fileContent));
	}
	
}
