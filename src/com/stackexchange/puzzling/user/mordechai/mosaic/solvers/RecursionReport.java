package com.stackexchange.puzzling.user.mordechai.mosaic.solvers;

public class RecursionReport implements Report {

	private final long elapsed;
	private final int steps;
	private final int deepestRecursion;
	private final int backTracks;

	public RecursionReport(long elapsed, int steps, int deepestRecursion, int backTracks) {
		this.elapsed = elapsed;
		this.steps = steps;
		this.deepestRecursion = deepestRecursion;
		this.backTracks = backTracks;
	}

	@Override
	public long getElapsed() {
		return elapsed;
	}

	@Override
	public int getSteps() {
		return steps;
	}

	public int getDeepestRecursion() {
		return deepestRecursion;
	}

	public int getBackTracks() {
		return backTracks;
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
		
		b.append("Deepest Recursion Level: ");
		b.append(getDeepestRecursion());
		b.append("\n");
		
		b.append("BackTrack Amount: ");
		b.append(getBackTracks());
		b.append("\n");
		
		return b.toString();
		
		
	}

}
