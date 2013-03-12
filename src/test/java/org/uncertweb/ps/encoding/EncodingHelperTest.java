package org.uncertweb.ps.encoding;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.Closeable;
import java.util.Set;
import java.util.jar.JarInputStream;

import org.junit.Test;

public class EncodingHelperTest {

	@Test
	public void getInterfaces() {
		Set<Class<?>> interfaces = EncodingHelper.getInterfaces(JarInputStream.class);
		assertThat(interfaces.size(), equalTo(2));
		assertThat(interfaces.contains(Closeable.class), equalTo(true));
		assertThat(interfaces.contains(AutoCloseable.class), equalTo(true));
	}
	
}
