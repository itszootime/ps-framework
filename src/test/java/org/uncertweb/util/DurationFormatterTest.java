package org.uncertweb.util;

import junit.framework.Assert;

import org.junit.Test;

public class DurationFormatterTest {
	
	@Test
	public void millis() {
		Assert.assertEquals("0.989s", DurationFormatter.format(989));
		Assert.assertEquals("0.001s", DurationFormatter.format(1));
	}
	
	@Test
	public void seconds() {
		Assert.assertEquals("23s", DurationFormatter.format(23001));
		Assert.assertEquals("23.01s", DurationFormatter.format(23010));
		Assert.assertEquals("23.01s", DurationFormatter.format(23014)); // round down
		Assert.assertEquals("23.02s", DurationFormatter.format(23015)); // round up
		Assert.assertEquals("23.1s", DurationFormatter.format(23100));
		Assert.assertEquals("23.25s", DurationFormatter.format(23250));
	}
	
	@Test
	public void minutes() {
		Assert.assertEquals("2m", DurationFormatter.format(120000));
		Assert.assertEquals("2m5s", DurationFormatter.format(125000));
		Assert.assertEquals("2m11s", DurationFormatter.format(131050));
		Assert.assertEquals("2m12s", DurationFormatter.format(131500)); // round up
	}
	
	@Test
	public void hours() {
		long minute = 1000 * 60;
		long hour = minute * 60;
		Assert.assertEquals("5h", DurationFormatter.format(5 * hour));
		Assert.assertEquals("5h30m", DurationFormatter.format(5 * hour + 30 * minute));
		Assert.assertEquals("5h7m", DurationFormatter.format(5 * hour + 7 * minute + 30000)); // shouldn't round up
	}

}
