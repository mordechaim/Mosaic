package com.stackexchange.puzzling.user.mordechai.mosaic.solvers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.stackexchange.puzzling.user.mordechai.grid.GridIterator;
import com.stackexchange.puzzling.user.mordechai.mosaic.Clue;
import com.stackexchange.puzzling.user.mordechai.mosaic.Mosaic;
import static com.stackexchange.puzzling.user.mordechai.mosaic.solvers.StandardState.*;

public abstract class AbstractSolveAlgorithm implements SolveAlgorithm {

	private Mosaic mosaic;
	private AtomicReference<State> state;

	private Map<State, List<Consumer<SolveAlgorithm>>> singleStateListeners;
	private List<Consumer<SolveAlgorithm>> allStateListeners;

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
			GridIterator<Clue> gi = getMosaic().grid().iterator();
			while (gi.hasNext()) {
				gi.next();
				if (getMosaic().grid().getSurroundingCells(gi.x(), gi.y())
						.count(clue -> clue.getClue() >= 0) == 0) {
					setState(FAILED);
					throw new NoClueException(getMosaic(), gi.x(), gi.y());
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

		fire(allStateListeners);
		fire(singleStateListeners, newState);
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
	public void addStateListener(State state, Consumer<SolveAlgorithm> handler) {
		if (singleStateListeners == null) {
			singleStateListeners = new HashMap<>();
		}

		List<Consumer<SolveAlgorithm>> listeners = singleStateListeners.get(state);

		if (listeners == null) {
			listeners = new ArrayList<>();
			singleStateListeners.put(state, listeners);
		}

		listeners.add(handler);
	}

	@Override
	public void removeStateListener(State state, Consumer<SolveAlgorithm> handler) {
		if (singleStateListeners == null)
			return;

		List<Consumer<SolveAlgorithm>> listeners = singleStateListeners.get(state);
		if (listeners == null)
			return;

		listeners.remove(handler);

		if (listeners.isEmpty())
			singleStateListeners.remove(state);

	}

	@Override
	public void addStateListener(Consumer<SolveAlgorithm> handler) {
		if (allStateListeners == null)
			allStateListeners = new ArrayList<>();

		allStateListeners.add(handler);
	}

	@Override
	public void removeStateListener(Consumer<SolveAlgorithm> handler) {
		if (allStateListeners == null)
			return;

		allStateListeners.remove(handler);
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
		if (handler != null)
			handler.accept(this);
	}

	private void fire(List<Consumer<SolveAlgorithm>> listeners) {
		if (listeners != null)
			listeners.forEach(h -> fire(h));
	}

	private void fire(Map<State, List<Consumer<SolveAlgorithm>>> map, State state) {
		if (map != null)
			fire(map.get(state));
	}

	protected void startTimer() {
		startTime = System.currentTimeMillis();
	}

	protected void done() {
		endTime = System.currentTimeMillis();
	}

}
