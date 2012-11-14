package org.uncertweb.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class DurationFormatterTest {
	
	@Test
	public void millis() {
		assertThat(DurationFormatter.format(1), equalTo("0.001s"));
	}
	
	@Test
	public void seconds() {
		assertThat(DurationFormatter.format(23001), equalTo("23s"));
	}
	
	@Test
	public void secondsWithMillis() {
		assertThat(DurationFormatter.format(23100), equalTo("23.1s"));
	}
	
	@Test
	public void secondsWithMillisRoundDown() {
		assertThat(DurationFormatter.format(23014), equalTo("23.01s"));
	}
	
	@Test
	public void secondsWithMillisRoundUp() {
		assertThat(DurationFormatter.format(23015), equalTo("23.02s"));
	}
	
	@Test
	public void minutes() {
		assertThat(DurationFormatter.format(120000), equalTo("2m"));
	}
	
	@Test
	public void minutesWithSeconds() {
		assertThat(DurationFormatter.format(125000), equalTo("2m5s"));
	}
	
	@Test
	public void minutesWithSecondsAndMillisRoundDown() {
		assertThat(DurationFormatter.format(131050), equalTo("2m11s"));
	}
	
	@Test
	public void minutesWithSecondsAndMillisRoundUp() {
		assertThat(DurationFormatter.format(131500), equalTo("2m12s"));
	}
	
	@Test
	public void hours() {
		long duration = TimeUnit.HOURS.toMillis(5);
		assertThat(DurationFormatter.format(duration), equalTo("5h"));
	}
	
	@Test
	public void hoursWithMinutes() {
		long duration = TimeUnit.HOURS.toMillis(5) + TimeUnit.MINUTES.toMillis(30);
		assertThat(DurationFormatter.format(duration), equalTo("5h30m"));
	}
	
	@Test
	public void hoursWithMinutesAndSecondsNoRound() {
		long duration = TimeUnit.HOURS.toMillis(5) + TimeUnit.MINUTES.toMillis(7) + 30000;
		assertThat(DurationFormatter.format(duration), equalTo("5h7m"));
	}

}
