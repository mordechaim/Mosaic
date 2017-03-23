package com.stackexchange.puzzling.user.mordechai.mosaic.solvers;

import java.util.function.Consumer;

public interface SolveAlgorithm {
	
	void start();

	void cancel();

	void step();

	void pause();

	void resume();

	State getState();
	
	Report getReport();

	void onStart(Consumer<SolveAlgorithm> handler);

	Consumer<SolveAlgorithm> getOnStart();

	void onCancel(Consumer<SolveAlgorithm> handler);

	Consumer<SolveAlgorithm> getOnCancel();

	void onPause(Consumer<SolveAlgorithm> handler);

	Consumer<SolveAlgorithm> getOnPause();

	void onResume(Consumer<SolveAlgorithm> handler);

	Consumer<SolveAlgorithm> getOnResume();

	void onSucceed(Consumer<SolveAlgorithm> handler);

	Consumer<SolveAlgorithm> getOnSucceed();
	
	void onFail(Consumer<SolveAlgorithm> handler);

	Consumer<SolveAlgorithm> getOnFail();
	
	void onStateChange(Consumer<SolveAlgorithm> handler);

	Consumer<SolveAlgorithm> getOnStateChange();

}
