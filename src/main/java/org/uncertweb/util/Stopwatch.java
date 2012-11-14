package org.uncertweb.util;



public class Stopwatch {

	private long start;
	
	public Stopwatch() {
		reset();
	}
	
	public String getElapsedTime() {
		long duration = System.currentTimeMillis() - start;
		return DurationFormatter.format(duration);
	}
	
	public void reset() {
		start = System.currentTimeMillis();
	}
	
}
