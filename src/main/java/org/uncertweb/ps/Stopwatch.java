package org.uncertweb.ps;


public class Stopwatch {

	private long start;
	
	public Stopwatch() {
		reset();
	}
	
	public String getElapsedTime() {
		return ((System.nanoTime() - start) / 1000000000.0) + "s";
	}
	
	public void reset() {
		start = System.nanoTime();
	}
	
}
