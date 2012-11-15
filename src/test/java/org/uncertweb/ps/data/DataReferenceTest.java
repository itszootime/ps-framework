package org.uncertweb.ps.data;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;
public class DataReferenceTest {
	
	@Test
	public void constructURL() throws MalformedURLException {
		DataReference ref = new DataReference(new URL("http://com.com"));
		assertThat(ref.getURL(), equalTo(new URL("http://com.com")));
		assertThat(ref.getMimeType(), nullValue());
		assertThat(ref.isCompressed(), equalTo(false));
	}
	
	@Test
	public void constructURLMimeType() throws MalformedURLException {
		DataReference ref = new DataReference(new URL("http://com.com"), "text/xml");
		assertThat(ref.getURL(), equalTo(new URL("http://com.com")));
		assertThat(ref.getMimeType(), equalTo("text/xml"));
		assertThat(ref.isCompressed(), equalTo(false));
	}
	
	@Test
	public void constructURLMimeTypeCompression() throws MalformedURLException {
		DataReference ref = new DataReference(new URL("http://com.com"), "text/xml", true);
		assertThat(ref.getURL(), equalTo(new URL("http://com.com")));
		assertThat(ref.getMimeType(), equalTo("text/xml"));
		assertThat(ref.isCompressed(), equalTo(true));
	}
	
	@Test
	public void constructURLCompression() throws MalformedURLException {
		DataReference ref = new DataReference(new URL("http://com.com"), true);
		assertThat(ref.getURL(), equalTo(new URL("http://com.com")));
		assertThat(ref.getMimeType(), nullValue());
		assertThat(ref.isCompressed(), equalTo(true));
	}

}
