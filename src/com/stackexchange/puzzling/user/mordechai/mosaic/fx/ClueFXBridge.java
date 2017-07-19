package com.stackexchange.puzzling.user.mordechai.mosaic.fx;

import com.stackexchange.puzzling.user.mordechai.mosaic.Clue;
import com.stackexchange.puzzling.user.mordechai.mosaic.Fill;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

public class ClueFXBridge extends Clue {

	private ReadOnlyObjectWrapper<Fill> fillProperty;
	private ReadOnlyIntegerWrapper clueProperty;
	private ReadOnlyBooleanWrapper isPixelProperty;

	public ClueFXBridge(Fill fill, int clue, boolean isPixel) {
		this();
		
		setFill(fill);
		setClue(clue);
		setIsPixel(isPixel);
	}

	public ClueFXBridge(boolean isPixel) {
		this();

		setIsPixel(isPixel);
	}

	public ClueFXBridge(Clue other) {
		this(other.getFill(), other.getClue(), other.isPixel());
	}

	public ClueFXBridge() {
		fillProperty = new ReadOnlyObjectWrapper<Fill>(this, "fill", Fill.EMPTY);
		clueProperty = new ReadOnlyIntegerWrapper(this, "clue", -1);
		isPixelProperty = new ReadOnlyBooleanWrapper(this, "isPixel", false);
	}

	@Override
	public ClueFXBridge copy() {
		return new ClueFXBridge(this);
	}

	public ReadOnlyObjectProperty<Fill> fillProperty() {
		return fillProperty.getReadOnlyProperty();
	}

	@Override
	public void setFill(Fill fill) {
		super.setFill(fill);

		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> fillProperty.set(fill));

			return;
		}

		fillProperty.set(fill);
	}

	public ReadOnlyIntegerProperty clueProperty() {
		return clueProperty.getReadOnlyProperty();
	}

	@Override
	public void setClue(int clue) {
		super.setClue(clue);

		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> clueProperty.set(clue));

			return;
		}

		clueProperty.set(clue);
	}

	public ReadOnlyBooleanProperty isPixelProperty() {
		return isPixelProperty.getReadOnlyProperty();
	}

	@Override
	public void setIsPixel(boolean flag) {
		super.setIsPixel(flag);

		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> isPixelProperty.set(flag));

			return;
		}

		isPixelProperty.set(flag);
	}

}
