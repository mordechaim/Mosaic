package com.stackexchange.puzzling.user.mordechai.mosaic.solvers;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.stackexchange.puzzling.user.mordechai.grid.GridIterator;
import com.stackexchange.puzzling.user.mordechai.mosaic.Clue;
import com.stackexchange.puzzling.user.mordechai.mosaic.Mosaic;
import static com.stackexchange.puzzling.user.mordechai.mosaic.solvers.StandardState.*;

public abstract class AbstractSolveAlgorithm implements SolveAlgorithm {

	private Mosaic mosaic;
	private AtomicReference<State> state;

	private Consumer<SolveAlgorithm> startHandler;
	private Consumer<SolveAlgorithm> cancelHandler;
	private Consumer<SolveAlgorithm> pauseHandler;
	private Consumer<SolveAlgorithm> resumeHandler;
	private Consumer<SolveAlgorithm> succeedHandler;
	private Consumer<SolveAlgorithm> failHandler;
	private Consumer<SolveAlgorithm> stateChangeHandler;

	private long startTime;
	private long endTime;

	public AbstractSolveAlgorithm(Mosaic mosaic, boolean checkNoClue) {
		state = new AtomicReference<State>(INITIALIZING);
		this.mosaic = mosaic;

		if (checkNoClue) {
			GridIterator<Clue> gi = getMosaic().getGrid().iterator();
			while (gi.hasNext()) {
				gi.next();
				if (getMosaic().getSurroundingCells(gi.getX(), gi.getY()).count(clue -> clue.getClue() >= 0) == 0) {
					setState(FAILED);
					throw new NoClueException(getMosaic(), gi.getX(), gi.getY());
				}
			}
		}
	}

	public Mosaic getMosaic() {
		return mosaic;
	}

	@Override
	public State getState() {
		return state.get();

	}

	protected void setState(State newState) {
		State old = state.get();
		if (old.equals(newState))
			return;

		state.set(newState);

		fire(getOnStateChange());

		if (newState instanceof StandardState) {
			switch ((StandardState) newState) {

			case RUNNING:
				if (old == READY) {
					fire(getOnStart());
				} else if (old == PAUSED) {
					fire(getOnResume());
				}
				break;

			case PAUSED:
				fire(getOnPause());
				break;

			case CANCELLED:
				fire(getOnCancel());
				break;

			case SUCCEEDED:
				fire(getOnSucceed());
				break;

			case FAILED:
				fire(getOnFail());
				break;

			default:
			}
		}
	}

	@Override
	public void onStart(Consumer<SolveAlgorithm> handler) {
		this.startHandler = handler;
	}

	@Override
	public Consumer<SolveAlgorithm> getOnStart() {
		return startHandler;
	}

	@Override
	public void onCancel(Consumer<SolveAlgorithm> handler) {
		this.cancelHandler = handler;
	}

	@Override
	public Consumer<SolveAlgorithm> getOnCancel() {
		return cancelHandler;
	}

	@Override
	public void onPause(Consumer<SolveAlgorithm> handler) {
		this.pauseHandler = handler;
	}

	@Override
	public Consumer<SolveAlgorithm> getOnPause() {
		return pauseHandler;
	}

	@Override
	public void onResume(Consumer<SolveAlgorithm> handler) {
		this.resumeHandler = handler;
	}

	@Override
	public Consumer<SolveAlgorithm> getOnResume() {
		return resumeHandler;
	}

	@Override
	public void onSucceed(Consumer<SolveAlgorithm> handler) {
		this.succeedHandler = handler;
	}

	@Override
	public Consumer<SolveAlgorithm> getOnSucceed() {
		return succeedHandler;
	}

	@Override
	public void onFail(Consumer<SolveAlgorithm> handler) {
		this.failHandler = handler;
	}

	@Override
	public Consumer<SolveAlgorithm> getOnFail() {
		return failHandler;
	}

	@Override
	public void onStateChange(Consumer<SolveAlgorithm> handler) {
		this.stateChangeHandler = handler;
	}

	@Override
	public Consumer<SolveAlgorithm> getOnStateChange() {
		return stateChangeHandler;
	}

	public long elapsed() {
		if (startTime == 0)
			return 0;
		
		if (endTime == 0)
			return System.currentTimeMillis() - startTime;

		return endTime - startTime;
	}

	private void fire(Consumer<SolveAlgorithm> handler) {
		if (handler == null)
			return;

		handler.accept(this);
	}

	protected void startTimer() {
		startTime = System.currentTimeMillis();
	}

	protected void done() {
		endTime = System.currentTimeMillis();
	}

}
