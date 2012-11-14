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

}
