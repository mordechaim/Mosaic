package com.stackexchange.puzzling.user.mordechai.mosaic.solvers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

import com.stackexchange.puzzling.user.mordechai.grid.Grid;
import com.stackexchange.puzzling.user.mordechai.grid.GridIterator;
import com.stackexchange.puzzling.user.mordechai.mosaic.Clue;
import com.stackexchange.puzzling.user.mordechai.mosaic.Mosaic;

import static com.stackexchange.puzzling.user.mordechai.mosaic.Fill.*;
import static com.stackexchange.puzzling.user.mordechai.mosaic.solvers.StandardState.*;

public class RecursionSolver extends AbstractSolveAlgorithm {

	private ListIterator<Coordinates> iterator;
	private List<Coordinates> clues;
	private List<Coordinates> loopbackClues;
	private List<RecursionSolver> children;
	private List<RecursionSolver> childrenUnmodifiable;

	private Coordinates currentPoint;
	private Coordinates recursionPoint;

	private boolean useAdvancedLogic;
	private boolean checkAmbiguity;
	private boolean loopbackEnhacement = true;
	private int level;

	private RecursionSolver parent;
	private Consumer<RecursionSolver> recursionHandler;

	private int recursions;
	private int steps;
	private int backtracks;

	public RecursionSolver(Mosaic mosaic) {
		this(mosaic, null, null, 0);
	}

	private RecursionSolver(Mosaic mosaic, List<Coordinates> coordinates, RecursionSolver parent, int level) {
		super(mosaic, coordinates == null);

		useAdvancedLogic = true;

		clues = new ArrayList<>();
		loopbackClues = new ArrayList<>();
		children = new ArrayList<>();

		if (coordinates != null) {
			for (Coordinates coords : coordinates) {
				if (!coords.solved)
					clues.add(new Coordinates(coords.x, coords.y));
			}
		} else {
			GridIterator<Clue> i = getMosaic().iterator();
			i.forEachRemaining(clue -> {
				if (clue.getClue() >= 0)
					clues.add(new Coordinates(i.x(), i.y()));
			});
		}

		this.parent = parent;
		this.level = level;
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
		children.clear();
	}

	private void run() {
		do {
			takeStep(false);
		} while (getState() == RUNNING);
	}

	public void onRecursion(Consumer<RecursionSolver> handler) {
		this.recursionHandler = handler;
	}

	public Consumer<RecursionSolver> getOnRecursion() {
		return recursionHandler;
	}

	private void takeStep(boolean pauseOnDone) {
		setState(RUNNING);

		if (elapsed() == 0)
			startTimer();

		if (!isLeaf()) {
			try {
				children.get(0).takeStep(pauseOnDone);
			} catch (AmbigiousException e) {
				if (getState() == RUNNING) // parents
					setState(FAILED);
				// Unwinds recursion
				throw e;
			} catch (IllegalClueStateException e) {
				backtracks++;

				// last child
				if (isSelfActive()) {

					// succeed if in validation
					if (gridComplete()) {
						setState(SUCCEEDED);
						return;
					}

					setState(FAILED);
					throw e; // last child threw, throw parent
				}
			}
		} else {
			if (gridComplete()) {
				assert getMosaic().count(clue -> clue.getFill() == EMPTY) == 0;

				setState(SUCCEEDED);
				return;
			}

			if (iterator == null)
				startIteration();

			// FIXME swap around so step does both at once
			if (iterator.hasNext() || !loopbackClues.isEmpty()) {
				takeStepImpl();
			} else {
				endIteration();
			}
		}

		if (pauseOnDone && isRunnable())
			pause();
	}

	private boolean changed;

	private void takeStepImpl() {
		steps++;

		boolean localChanged = false;
		Coordinates c = null;

		while (!loopbackClues.isEmpty()) {
			Coordinates coords = loopbackClues.remove(0);
			if (!coords.solved) {
				c = coords;
				break;
			}

		}

		if (c == null) {
			while (iterator.hasNext()) {
				Coordinates coords = iterator.next();
				if (!coords.solved) {
					c = coords;
					break;
				}
				iterator.remove();
			}
		}

		if (c == null)
			return;

		currentPoint = c;
		Grid<Clue> surrounding = getMosaic().getSurroundingCells(c.x, c.y);

		Clue clue = getMosaic().get(c.x, c.y);
		if (clue.getClue() < 0) {
			iterator.remove();
			return;
		}

		int filledAmt = surrounding.count(cl -> cl.getFill() == FILLED);
		int xAmt = surrounding.count(cl -> cl.getFill() == X);
		int emptyAmt = surrounding.count(cl -> cl.getFill() == EMPTY);

		if (filledAmt > clue.getClue()) {
			setState(FAILED);
			throw new ContradictionException(getMosaic(), c.x, c.y);
		}

		if (surrounding.length() - xAmt < clue.getClue()) {
			setState(FAILED);
			throw new ContradictionException(getMosaic(), c.x, c.y);
		}

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

		c.solved = surrounding.count(cl -> cl.getFill() == EMPTY) == 0;

		if (localChanged && isUsingLoopbackEnhancment()) {
			int previousCount = 0;
			// loops back 2 or 3 rows, then adds all that are max 2 cells
			// while reseting
			// iterator to previous position
			while (iterator.hasPrevious()) {
				Coordinates p = iterator.previous();
				previousCount++;
				if ((p.y == c.y - 2 && p.x < c.x - 2) || p.y < c.y - 2) {
					break;
				}
			}
			while (previousCount > 0) {
				Coordinates n = iterator.next();
				previousCount--;
				if (n != c && n.x >= c.x - 2 && n.x <= c.x + 2 && n.y >= c.y - 2 && n.y <= c.y + 2) {
					loopbackClues.add(n);
				}

				if (n.x >= c.x && n.y >= c.y)
					break;
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
		if ((!changed || isUsingLoopbackEnhancment()) && !gridComplete()) {

			Coordinates bc = null;

			while (!clues.isEmpty()) {
				bc = clues.get(0);
				if (!bc.solved)
					break;

				clues.remove(0);
			}

			if(gridComplete()) { // may happen on above removal
				return;
			}

			recursionPoint = bc;

			int currentClue = getMosaic().get(bc.x, bc.y).getClue();

			if (!isUsingAdvancedLogic())
				throw new RequiresAdvancedLogicException(getMosaic(), bc.x, bc.y);

			Grid<Clue> subgrid = getMosaic().getSurroundingCells(bc.x, bc.y);
			List<Integer> empties = new ArrayList<>();

			int filled = subgrid.count(clue -> clue.getFill() == FILLED);
			for (int i = 0; i < subgrid.length(); i++) {
				if (subgrid.get(i).getFill() == EMPTY) {
					empties.add(i);
				}
			}

			for (int i = 0; i <= empties.size() - (currentClue - filled); i++) {

				Mosaic m = new Mosaic(getMosaic());
				Grid<Clue> sub = m.getSurroundingCells(bc.x, bc.y);

				// x out all preceding already taken care in preceding children
				for (int j = 0; j < i; j++) {
					sub.get(empties.get(j)).setFill(X);
				}

				if (empties.isEmpty()) // may happen on loop-back
					return;

				sub.get(empties.get(i)).setFill(FILLED);

				RecursionSolver rs = new RecursionSolver(m, clues, this, level + 1);
				rs.checkAmbiguity(isCheckingAmbiguity());
				rs.addStateListener(SUCCEEDED, algorithm -> {

					if (inValidationProcess()) {
						setState(FAILED);
						throw new AmbigiousException(getMosaic(), rs.getMosaic(), recursionPoint.x, recursionPoint.y);
					}

					for (int x = 0; x < getMosaic().length(); x++) {
						getMosaic().get(x).setFill(rs.getMosaic().get(x).getFill());
					}

					rs.getMosaic().grid().forEach((data, x, y) -> getMosaic().get(x, y).setFill(data.getFill()));

					children.remove(rs);
					clues.clear(); // mark gridComplete
					iterator = null; // may not reflect clearance

					if (!inValidationProcess()) {
						children.clear();
						setState(SUCCEEDED);

						// Report updating
						recursions = rs.recursions + 1;
						steps += rs.steps;
						backtracks += rs.backtracks;
					}
				});

				rs.addStateListener(FAILED, algrithm -> {
					children.remove(rs);
					steps += rs.steps;
					backtracks += rs.backtracks;
				});

				children.add(rs);
			}

			Consumer<RecursionSolver> recursionHandler = getOnRecursion();
			if (recursionHandler != null) {
				recursionHandler.accept(this);
			}
		}
	}

	public boolean isRunnable() {
		State s = getState();
		return s == READY || s == RUNNING || s == PAUSED;
	}

	public boolean isTerminated() {
		State s = getState();
		return s == CANCELLED || s == FAILED || s == SUCCEEDED;
	}

	public void checkAmbiguity(boolean check) {
		State state = getState();
		if (state != INITIALIZING && state != READY)
			throw new IllegalStateException("State must be INITIALIZING or READY to toggle ambiguity check.");

		checkAmbiguity = check;
	}

	public boolean isCheckingAmbiguity() {
		return checkAmbiguity;
	}

	public void useAdvancedLogic(boolean logic) {
		State state = getState();
		if (state != INITIALIZING && state != READY)
			throw new IllegalStateException("State must be INITIALIZING or READY to toggle advanced logic usage.");

		useAdvancedLogic = logic;
	}

	public boolean isUsingAdvancedLogic() {
		return useAdvancedLogic;
	}

	public void useLoopbackEnhancement(boolean loopback) {
		State state = getState();
		if (state != INITIALIZING && state != READY)
			throw new IllegalStateException(
					"State must be INITIALIZING or READY to toggle loopback enhancement usage.");

		loopbackEnhacement = loopback;
	}

	public boolean isUsingLoopbackEnhancment() {
		return loopbackEnhacement;
	}

	public RecursionSolver getActive() {
		if (isTerminated())
			return null;
		if (isLeaf())
			return this;

		RecursionSolver s = getActiveChild();
		while (!s.isLeaf()) {
			s = s.getActiveChild();
		}

		return s;
	}

	public List<RecursionSolver> getChildren() {
		if (childrenUnmodifiable == null)
			childrenUnmodifiable = Collections.unmodifiableList(children);

		return childrenUnmodifiable;
	}

	public RecursionSolver getActiveChild() {
		if (isTerminated())
			return null;
		if (isLeaf())
			return this;

		return children.get(0);
	}

	public boolean isSelfActive() {
		return children.isEmpty() && isRunnable();
	}

	public RecursionSolver getParent() {
		return parent;
	}

	public RecursionSolver getRoot() {
		if (parent == null)
			return this;

		return parent.getRoot();
	}

	public boolean isRoot() {
		return getParent() == null;
	}

	public boolean isLeaf() {
		return children.isEmpty();
	}

	public int recursionLevel() {
		return level;
	}

	// FIXME to be removed or redesigned
	public Coordinates currentPoint() {
		if (isSelfActive())
			return currentPoint;

		return getActive() != null ? getActive().currentPoint : null;
	}

	public Coordinates recursionPoint() {
		return recursionPoint;
	}

	@Override
	protected void setState(State s) {
		State old = getState();

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
		return gridComplete() && isCheckingAmbiguity() && !isLeaf() && isRunnable();
	}

	private boolean gridComplete() {
		return clues.isEmpty();
	}

	@Override
	public RecursionReport getReport() {
		throw new UnsupportedOperationException();
		// return new RecursionReport(elapsed(), steps, recursions, backtracks);
	}

	public static class Coordinates {
		public final int x;
		public final int y;

		// internally mutable
		private boolean solved;

		public Coordinates(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return x + ", " + y;
		}
	}

	public static RecursionSolver commonParent(RecursionSolver rs1, RecursionSolver rs2) {
		if (rs1.level > rs2.level)
			return commonParentImpl(rs2, rs1);
		return commonParentImpl(rs1, rs2);
	}

	public static RecursionSolver commonParentImpl(RecursionSolver min, RecursionSolver max) {
		while (max.level > min.level)
			max = max.getParent();

		do {
			if (max == min)
				return min;

			min = min.getParent();
			max = max.getParent();
		} while (min != null);

		return min;
	}
}
