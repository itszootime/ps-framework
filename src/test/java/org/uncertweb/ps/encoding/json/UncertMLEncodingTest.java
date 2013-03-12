package org.uncertweb.ps.encoding.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Before;
import org.junit.Test;
import org.uncertml.distribution.categorical.CategoricalUniformDistribution;
import org.uncertml.distribution.continuous.NormalDistribution;
import org.uncertml.sample.RandomSample;
import org.uncertml.statistic.Mean;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.ParseException;

public class UncertMLEncodingTest {

	private UncertMLEncoding encoding;

	@Before
	public void before() {
		encoding = new UncertMLEncoding();
	}
	
	@Test
	public void supportedTypes() {
		Class<?>[] types = { CategoricalUniformDistribution.class, Mean.class,
				RandomSample.class, NormalDistribution.class }; // a few random ones
		for (Class<?> type : types) {
			assertThat(encoding.isSupportedType(type), equalTo(true));
		}
	}
	
	@Test
	public void supportedMimeType() {
		assertThat(encoding.isSupportedMimeType("application/json"), equalTo(true));
	}
	
	@Test
	public void defaultMimeType() {
		assertThat(encoding.getDefaultMimeType(), equalTo("application/json"));
	}
	
	@Test
	public void encode() throws EncodeException {
		Mean mean = new Mean(123.4);
		String encoded = encoding.encode(mean);
		assertThat(encoded, notNullValue());
		assertThat(encoded, containsString("Mean"));
	}
	
	@Test
	public void parse() throws ParseException {
		String encoded = "{\"Mean\":{\"values\":[123.4]}}";
		Mean mean = encoding.parse(encoded, Mean.class);
		assertThat(mean, notNullValue());
		assertThat(mean.getValues().size(), equalTo(1));
		assertThat(mean.getValues().get(0), equalTo(123.4));
	}
	
}
