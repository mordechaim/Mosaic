package com.stackexchange.puzzling.user.mordechai.mosaic.fx;

import com.stackexchange.puzzling.user.mordechai.mosaic.solvers.State;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Global {

	private Global() {
	}

	private static final Global INSTANCE = new Global();

	private ObjectProperty<MosaicPane> mosaicPane;
	private BooleanProperty autoSolving;
	private BooleanProperty generating;
	private BooleanProperty offCanvasOperating;
	private ObjectProperty<State> solverState;

	public ObjectProperty<MosaicPane> mosaicPaneProperty() {
		if (mosaicPane == null) {
			mosaicPane = new SimpleObjectProperty<MosaicPane>(this, "mosaicPane") {

				private void set(MosaicPane mp, boolean clear) {
					if (clear && mp != null && get() != null) {
						super.set(null); // clears solver and other cleanups
					}

					super.set(mp);
				}

				@Override
				public void set(MosaicPane mp) {
					set(mp, true);
				}

				@Override
				public void setValue(MosaicPane mp) {
					set(mp, false);
				}
			};
		}

		return mosaicPane;
	}

	public final MosaicPane getMosaicPane() {
		return mosaicPaneProperty().get();
	}

	public final void setMosaicPane(MosaicPane mosaic) {
		mosaicPaneProperty().set(mosaic);
	}

	public ObjectProperty<State> solverStateProperty() {
		if (solverState == null)
			solverState = new SimpleObjectProperty<>(this, "solverState");

		return solverState;
	}

	public final State getSolverState() {
		return solverStateProperty().get();
	}

	public void setSolverState(State state) {
		solverStateProperty().set(state);
	}

	public BooleanProperty autoSolvingProperty() {
		if (autoSolving == null) {
			autoSolving = new SimpleBooleanProperty(this, "autoSolving");
		}

		return autoSolving;
	}

	public final boolean isAutoSolving() {
		return autoSolvingProperty().get();
	}

	public final void setAutoSolving(boolean autoSolving) {
		autoSolvingProperty().set(autoSolving);
	}

	public BooleanProperty generatingProperty() {
		if (generating == null) {
			generating = new SimpleBooleanProperty(this, "generating");
		}

		return generating;
	}

	public final boolean isGenerating() {
		return generatingProperty().get();
	}

	public final void setGenerating(boolean generating) {
		generatingProperty().set(generating);
	}

	public BooleanProperty offCanvasOperatingProperty() {
		if (offCanvasOperating == null) {
			offCanvasOperating = new SimpleBooleanProperty(this, "offCanvasOperating");
		}

		return offCanvasOperating;
	}

	public final boolean isOffCanvasOperating() {
		return offCanvasOperatingProperty().get();
	}

	public final void setOffCanvasOperating(boolean bool) {
		offCanvasOperatingProperty().set(bool);
	}

	public static Global getInstance() {
		return INSTANCE;
	}

}
