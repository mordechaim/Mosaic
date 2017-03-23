package com.stackexchange.puzzling.user.mordechai.mosaic.solvers;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.stackexchange.puzzling.user.mordechai.grid.Grid;
import com.stackexchange.puzzling.user.mordechai.grid.GridIterator;
import com.stackexchange.puzzling.user.mordechai.mosaic.Clue;
import com.stackexchange.puzzling.user.mordechai.mosaic.Mosaic;

import static com.stackexchange.puzzling.user.mordechai.mosaic.Fill.*;
import static com.stackexchange.puzzling.user.mordechai.mosaic.solvers.StandardState.*;

public class RecursionSolver extends AbstractSolveAlgorithm {

	private ListIterator<Coordinates> iterator;
	private List<Coordinates> clues;
	private List<RecursionSolver> brutes;

	private boolean changed;

	private boolean checkAmbiguity;
	private int deepestRecursion;
	private int steps;
	private int backTracks;

	public RecursionSolver(Mosaic mosaic) {
		this(mosaic, null);
	}

	private RecursionSolver(Mosaic mosaic, List<Coordinates> coordinates) {
		super(mosaic, coordinates == null);

		clues = new ArrayList<>();
		brutes = new ArrayList<>();

		if (coordinates != null) {
			clues.addAll(coordinates);
		} else {
			GridIterator<Clue> i = getMosaic().getGrid().iterator();
			i.forEachRemaining(clue -> {
				if (clue.getClue() >= 0)
					clues.add(new Coordinates(i.getX(), i.getY()));
			});
		}

		setState(READY);
	}

	@Override
	public void start() {
		if (getState() != READY)
			throw new IllegalStateException("State must be READY to start().");

		run();
	}

	@Override
	public void step() {
		takeStep(true);
	}

	@Override
	public void pause() {
		if (getState() != RUNNING)
			throw new IllegalStateException("State must be RUNNING to pause().");

		setState(PAUSED);
	}

	@Override
	public void resume() {
		if (getState() != PAUSED)
			throw new IllegalStateException("State must be PAUSED to resume().");

		run();
	}

	@Override
	public void cancel() {
		setState(CANCELLED);
		clues.clear();
		brutes.clear();
	}

	private void run() {
		do {
			takeStep(false);
		} while (getState() == RUNNING || (inValidationProcess()));
	}

	private void takeStep(boolean pauseOnDone) {
		setState(RUNNING);

		if (elapsed() == 0)
			startTimer();

		if (!brutes.isEmpty()) {
			try {
				RecursionSolver rs;
				while (!brutes.isEmpty()) {
					rs = brutes.get(0);
					if (rs.isTerminated()) {
						// only happens when checking ambiguity
						brutes.remove(0);
					} else {
						rs.takeStep(pauseOnDone);
						break;
					}
				}
			} catch (AmbigiousException e) {
				setState(FAILED);

				// Unwinds recursion
				throw e;
			} catch (IllegalClueStateException e) {
				brutes.remove(0);
				backTracks++;
			}
		} else {

			if (clues.isEmpty()) {
				setState(SUCCEEDED);
				return;
			}

			if (iterator == null)
				startIteration();

			if (iterator.hasNext()) {
				takeStepImpl();
			} else
				endIteration();
		}

		if (pauseOnDone && !isTerminated())
			pause();
	}

	private void takeStepImpl() {
		steps++;
		boolean localChanged = false;

		if (iterator.hasNext()) {
			Coordinates c = iterator.next();

			Clue clue = getMosaic().getGrid().get(c.x, c.y);
			Grid<Clue> surrounding = getMosaic().getSurroundingCells(c.x, c.y);

			int filledAmt = surrounding.count(cl -> cl.getFill() == FILLED);
			int xAmt = surrounding.count(cl -> cl.getFill() == X);

			if (filledAmt > clue.getClue()) {
				setState(FAILED);
				throw new ContradictionException(getMosaic(), c.x, c.y);
			}
			if (9 - xAmt < clue.getClue()) {
				setState(FAILED);
				throw new ContradictionException(getMosaic(), c.x, c.y);
			}

			int emptyAmt = surrounding.count(cl -> cl.getFill() == EMPTY);
			if (emptyAmt + filledAmt == clue.getClue()) {
				for (Clue cl : surrounding) {
					if (cl.getFill() == EMPTY) {
						cl.setFill(FILLED);
						localChanged = true;
					}
				}
			}

			if (filledAmt == clue.getClue()) {
				for (Clue cl : surrounding) {
					if (cl.getFill() == EMPTY) {
						cl.setFill(X);
						localChanged = true;
					}
				}
			}

			if (surrounding.count(cl -> cl.getFill() == EMPTY) == 0) {
				iterator.remove();
			}

		}
		changed |= localChanged;
	}

	private void startIteration() {
		iterator = clues.listIterator();
		changed = false;
	}

	private void endIteration() {
		iterator = null;
		if (!changed && !clues.isEmpty()) {

			Coordinates bc = clues.get(0);
			Grid<Clue> subgrid = getMosaic().getSurroundingCells(bc.x, bc.y);
			List<Integer> empties = new ArrayList<>();

			for (int i = 0; i < subgrid.getLength(); i++) {
				if (subgrid.get(i).getFill() == EMPTY)
					empties.add(i);
			}

			for (int i : empties) {
				Mosaic m = new Mosaic(getMosaic());
				Grid<Clue> sub = m.getSurroundingCells(bc.x, bc.y);
				sub.get(i).setFill(FILLED);

				RecursionSolver rs = new RecursionSolver(m, clues);
				rs.checkAmbiguity(isCheckingAmbiguity());
				rs.onSucceed(algorithm -> {
					if (getState() == SUCCEEDED) {
						int xDiff = 0;
						int yDiff = 0;

						GridIterator<Clue> gi = getMosaic().getGrid().iterator();
						while (gi.hasNext()) {
							if (gi.next().getFill() != rs.getMosaic().getGrid().get(gi.getX(), gi.getY()).getFill()) {
								xDiff = gi.getX();
								yDiff = gi.getY();
								break;
							}
						}

						throw new AmbigiousException(getMosaic(), rs.getMosaic(), xDiff, yDiff);
					}
					getMosaic().getGrid().fill((x, y, old) -> rs.getMosaic().getGrid().get(x, y));

					// Report updating
					int deepest = rs.deepestRecursion + 1;
					deepestRecursion = Math.max(deepestRecursion, deepest);
					steps += rs.steps;
					backTracks += rs.backTracks;

					setState(SUCCEEDED);
				});

				brutes.add(rs);
			}
		}

	}

	public boolean isRunnable() {
		State s = getState();
		return s == READY || s == RUNNING || s == PAUSED || inValidationProcess();
	}

	public boolean isTerminated() {
		State s = getState();
		return s == CANCELLED || s == FAILED || (s == SUCCEEDED && !inValidationProcess());
	}

	public void checkAmbiguity(boolean check) {
		if (checkAmbiguity == check)
			return;

		checkAmbiguity = check;
		for (RecursionSolver a : brutes)
			a.checkAmbiguity(check);
	}

	public boolean isCheckingAmbiguity() {
		return checkAmbiguity;
	}

	@Override
	protected void setState(State s) {
		State old = getState();

		if (inValidationProcess()) {
			if (s == FAILED) // only legal value on validation
				super.setState(s);

			return;
		}

		if (old == CANCELLED)
			throw new IllegalStateException("Can't resume after cancel().");
		if (old == FAILED)
			throw new IllegalStateException("Can't resume after exception.");
		if (old == SUCCEEDED) {
			throw new IllegalStateException("Puzzle already completed.");
		}

		super.setState(s);

		if (isTerminated())
			done();
	}

	private boolean inValidationProcess() {
		return getState() == SUCCEEDED && (isCheckingAmbiguity() && !brutes.isEmpty());
	}

	@Override
	public RecursionReport getReport() {
		return new RecursionReport(elapsed(), steps, deepestRecursion, backTracks);
	}

	static class Coordinates {
		final int x;
		final int y;

		Coordinates(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public String toString() {
			return x + ", " + y;
		}
	}

}
