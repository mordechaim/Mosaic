package com.stackexchange.puzzling.user.mordechai.mosaic.solvers;

public class RecursionReport implements Report {

	private final long elapsed;
	private final int steps;
	private final int recursions;
	private final int backtracks;

	public RecursionReport(long elapsed, int steps, int recursions, int backtracks) {
		this.elapsed = elapsed;
		this.steps = steps;
		this.recursions = recursions;
		this.backtracks = backtracks;
	}

	@Override
	public long getElapsed() {
		return elapsed;
	}

	@Override
	public int getSteps() {
		return steps;
	}

	public int getRecursions() {
		return recursions;
	}

	public int getBacktracks() {
		return backtracks;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		
		b.append("Time Elapsed: ");
		double elapsed = getElapsed();
		b.append(elapsed / 1000);
		b.append(" seconds\n");
		
		b.append("Steps: ");
		b.append(getSteps());
		b.append("\n");
		
		b.append("Recursions: ");
		b.append(getRecursions());
		b.append("\n");
		
		b.append("Backtracks: ");
		b.append(getBacktracks());
		b.append("\n");
		
		return b.toString();
		
		
	}

}
