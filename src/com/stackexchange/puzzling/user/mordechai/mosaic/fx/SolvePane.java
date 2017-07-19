package com.stackexchange.puzzling.user.mordechai.mosaic.fx;

import static com.stackexchange.puzzling.user.mordechai.mosaic.solvers.StandardState.PAUSED;
import static com.stackexchange.puzzling.user.mordechai.mosaic.solvers.StandardState.RUNNING;

import java.util.function.Consumer;

import com.stackexchange.puzzling.user.mordechai.mosaic.Clue;
import com.stackexchange.puzzling.user.mordechai.mosaic.Mosaic;
import com.stackexchange.puzzling.user.mordechai.mosaic.solvers.IllegalClueStateException;
import com.stackexchange.puzzling.user.mordechai.mosaic.solvers.RecursionSolver;
import com.stackexchange.puzzling.user.mordechai.mosaic.solvers.RecursionSolver.Coordinates;
import com.stackexchange.puzzling.user.mordechai.mosaic.solvers.SolveAlgorithm;
import com.stackexchange.puzzling.user.mordechai.mosaic.solvers.State;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SolvePane extends VBox {
	// TODO modulate these action listeners and others into general methods

	private Timeline runner;

	private Slider delay;

	private Button complete;
	private Button step;
	private Button run;
	private Button stop;

	private CheckBox checkAmbiguity;
	private CheckBox loopbackEnhacement;

	private Label exceptionMessage;

	private ObjectProperty<RecursionSolver> solver;
	private StateObserver state;
	private Coordinates current;
	private Coordinates errorPoint;

	private Consumer<RecursionSolver> recursionHandler;
	private Consumer<SolveAlgorithm> backtrackHandler;
	private BooleanProperty completing;

	private BooleanBinding completingAndRunning;

	private static final Global global = Global.getInstance();

	public SolvePane() {
		super(5);

		solver = new SimpleObjectProperty<>(this, "solver");
		state = new StateObserver(solver);
		delay = new Slider(0.1, 2.5, 1.3);
		delay.setTooltip(new Tooltip("Solving speed"));

		runner = new Timeline(new KeyFrame(Duration.millis(60), evt -> step()));
		runner.setCycleCount(Timeline.INDEFINITE);
		runner.rateProperty().bind(delay.valueProperty());

		solver.addListener((obs, ov, nv) -> {
			if (nv == null) {
				current = null;
				errorPoint = null;

				// only necessary on single instance passing parameter
				runner.stop();
			}
		});

		completing = new SimpleBooleanProperty(false);

		global.mosaicPaneProperty().addListener((obs, ov, nv) -> {
			if (nv == null)
				cancel();
		});
		global.autoSolvingProperty().bind(runner.statusProperty().isEqualTo(Animation.Status.RUNNING).or(completing));
		global.solverStateProperty().bind(state.stateProperty());

		completingAndRunning = completing.and(state.stateProperty().isEqualTo(RUNNING));
		completingAndRunning.addListener((obs, ov, nv) -> global.setOffCanvasOperating(nv));

		step = new Button("Step");
		complete = new Button("Complete");
		run = new Button();
		stop = new Button();

		run.textProperty().bind(Bindings.when(state.stateProperty().isEqualTo(PAUSED)).then("Resume")
				.otherwise(Bindings.when(global.autoSolvingProperty()).then("Pause").otherwise("Run")));

		stop.textProperty().bind(Bindings.when(global.autoSolvingProperty()).then("Stop").otherwise("Restart"));

		step.disableProperty().bind(global.autoSolvingProperty());
		stop.disableProperty().bind(Bindings.createBooleanBinding(() -> {
			State s = state.getState();
			return s != RUNNING && s != PAUSED;
		}, state.stateProperty()));
		complete.disableProperty().bind(completing);

		step.setOnAction(evt -> step());
		complete.setOnAction(evt -> complete());
		run.setOnAction(evt -> {
			if (global.isAutoSolving()) {
				if (completing.get()) {
					if (state.getState() == RUNNING) {
						solver.get().pause();
					} else if (state.getState() == PAUSED) { // requires threading
						Thread t = new Thread(() -> {
							try {
								solver.get().resume();
							} catch (IllegalClueStateException e) {
								Platform.runLater(() -> handleException(e));
							}
						});
						t.setDaemon(true);
						t.start();
					}
				} else {
					runner.pause();
				}
			} else {
				runner.play();
			}
		});
		stop.setOnAction(evt -> cancel());

		step.setFocusTraversable(false);
		complete.setFocusTraversable(false);
		run.setFocusTraversable(false);
		stop.setFocusTraversable(false);

		step.getStyleClass().add("left");
		complete.getStyleClass().add("center");
		run.getStyleClass().add("center");
		stop.getStyleClass().add("right");

		checkAmbiguity = new CheckBox("Verify Uniqueness");
		checkAmbiguity.disableProperty().bind(stop.disabledProperty().not());
		checkAmbiguity.setTooltip(
				new Tooltip("Checks if the mosaic has a\n" + "unique solution.\n" + "May take longer to complete"));

		loopbackEnhacement = new CheckBox("Loopback Enhacement");
		loopbackEnhacement.setSelected(true);
		loopbackEnhacement.disableProperty().bind(checkAmbiguity.disableProperty());
		loopbackEnhacement.setTooltip(new Tooltip(
				"Speeds up solving by looping\n" + "back on every change instead of\n" + "linear iterations"));

		VBox checks = new VBox(checkAmbiguity, loopbackEnhacement);

		Pane spacer1 = new Pane();
		HBox.setHgrow(spacer1, Priority.ALWAYS);
		Pane spacer2 = new Pane();
		HBox.setHgrow(spacer2, Priority.ALWAYS);

		exceptionMessage = new Label();

		setAlignment(Pos.TOP_CENTER);

		HBox buttonPane = new HBox(spacer1, step, complete, run, stop, spacer2, checks);
		buttonPane.setAlignment(Pos.CENTER);

		setPadding(new Insets(10, 50, 0, 50));
		getChildren().addAll(buttonPane, delay, exceptionMessage);

		getStylesheets().add(getClass().getResource("resources/css/round-buttons.css").toExternalForm());

	}

	public void step() {
		try {
			setupSolver();
			removeHighlighting();

			solver.get().step();
		} catch (IllegalClueStateException e) {
			handleException(e);
			return;
		}

		current = solver.get().currentPoint();

		if (current != null) {
			ClueCell cc = global.getMosaicPane().getAt(current.x, current.y);
			cc.setHighlighted(true);
		}
	}

	public void cancel() {
		if (solver.get() != null && solver.get().isRunnable()) {
			solver.get().cancel();
			completing.set(false);
		}

		removeHighlighting();
		solver.set(null);
	}

	public void complete() {
		try {
			setupSolver();
			removeHighlighting();

		} catch (IllegalClueStateException e) {
			handleException(e);
			return;
		}

		Thread tr = new Thread(() -> {
			Platform.runLater(() -> completing.set(true));
			try {
				Mosaic copy = new Mosaic(global.getMosaicPane().getMosaic().grid().branch(Clue::new));

				RecursionSolver s = new RecursionSolver(copy);
				s.checkAmbiguity(checkAmbiguity.isSelected());
				s.useLoopbackEnhancement(loopbackEnhacement.isSelected());

				s.onStateChange(solver -> {
					MosaicPane pane = global.getMosaicPane();

					if (s.isTerminated()) {
						Platform.runLater(() -> {
							completing.set(false);
							pane.getMosaic().grid()
									.forEach((clue, x, y) -> clue.setFill(s.getMosaic().get(x, y).getFill()));
						});
					} else if (s.getState() == PAUSED) {
						Platform.runLater(() -> {
							RecursionSolver active = s.getActive();
							if (active != null) { // may be null by bug
								pane.getMosaic().grid()
										.forEach((clue, x, y) -> clue.setFill(active.getMosaic().get(x, y).getFill()));
							}
						});
					}
				});
				solver.set(s);
				s.start();

			} catch (IllegalClueStateException e) {
				Platform.runLater(() -> handleException(e));
			}
		});

		tr.setDaemon(true);
		tr.start();
	}

	private void setupSolver() {
		if (solver.get() != null && solver.get().isRunnable())
			return;

		removeHighlighting();

		RecursionSolver rs = new RecursionSolver(global.getMosaicPane().getMosaic());
		solver.set(rs);
		rs.checkAmbiguity(checkAmbiguity.isSelected());
		rs.useLoopbackEnhancement(loopbackEnhacement.isSelected());
		recursionHandler = s -> {
			System.out.println("Recursion: " + (s.recursionLevel() + 1));
			s.getChildren().forEach(c -> c.onRecursion(recursionHandler));
			s.getChildren().forEach(c -> c.onFail(backtrackHandler));

			RecursionSolver active = s.getActive();
			global.mosaicPaneProperty().setValue(new MosaicPane(active.getMosaic()));
		};

		backtrackHandler = s -> {
			System.out.println("Backtrack: " + (((RecursionSolver) s).recursionLevel()));
			RecursionSolver active;
			if (rs.isTerminated())
				active = rs;
			else
				active = rs.getActive();

			global.mosaicPaneProperty().setValue(new MosaicPane(active.getMosaic()));

		};

		rs.onRecursion(recursionHandler);
		rs.onFail(backtrackHandler);
		rs.onStateChange(s -> {
			if (rs.isTerminated())
				runner.stop();
		});
	}

	public void removeHighlighting() {
		exceptionMessage.setText("");

		if (current != null && global.getMosaicPane() != null) {
			ClueCell cc = global.getMosaicPane().getAt(current.x, current.y);
			cc.setHighlighted(false);
		}

		if (errorPoint != null && global.getMosaicPane() != null) {
			ClueCell cc = global.getMosaicPane().getAt(errorPoint.x, errorPoint.y);
			cc.setError(false);
		}
	}

	private void handleException(IllegalClueStateException e) {
		errorPoint = new Coordinates(e.getX(), e.getY());
		global.getMosaicPane().getAt(e.getX(), e.getY()).setError(true);
		String message = e.getClass().getSimpleName();
		exceptionMessage.setText(message.substring(0, message.length() - "Exception".length()));
		runner.stop();
	}

	public static class StateObserver {

		private ReadOnlyObjectWrapper<State> state;
		private Consumer<SolveAlgorithm> listener;

		public StateObserver(Property<RecursionSolver> solver) {
			listener = alg -> {
				Platform.runLater(() -> state.set(alg.getState()));
			};

			state = new ReadOnlyObjectWrapper<>(this, "state");
			solver.addListener((obs, ov, nv) -> {
				Platform.runLater(() -> state.set(null));

				if (ov != null) {
					ov.removeStateListener(listener);
				}
				if (nv != null) {
					nv.addStateListener(listener);
				}

			});

		}

		public final State getState() {
			return state.get();
		}

		public ReadOnlyObjectProperty<State> stateProperty() {
			return state.getReadOnlyProperty();
		}
	}
}
