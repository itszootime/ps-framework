package org.uncertweb.test;

import junit.framework.Assert;

import org.joda.time.DateMidnight;

public class SupAssert {
	
	public static void assertDateEquals(int expectedYear, int expectedMonth, int expectedDay,
			String expectedTimeZoneId, DateMidnight actualDate) {
		Assert.assertEquals(expectedYear, actualDate.getYear());
		Assert.assertEquals(expectedMonth, actualDate.getMonthOfYear());
		Assert.assertEquals(expectedDay, actualDate.getDayOfMonth());
		Assert.assertEquals(expectedTimeZoneId, actualDate.getZone().getID());
	}
	
	public static void assertArrayEquals(Object[] expected, Object[] actual) {
		Assert.assertEquals(expected.length, actual.length);
		for (int i = 0; i < expected.length; i++) {
			Assert.assertEquals(expected[i], actual[i]);
		}
	}

}
