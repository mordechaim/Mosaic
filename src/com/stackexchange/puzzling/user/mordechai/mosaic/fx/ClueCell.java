package com.stackexchange.puzzling.user.mordechai.mosaic.fx;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;

import static com.stackexchange.puzzling.user.mordechai.mosaic.Fill.*;
import static com.stackexchange.puzzling.user.mordechai.mosaic.fx.EditorType.*;
import static com.stackexchange.puzzling.user.mordechai.mosaic.fx.Elevation.*;

public class ClueCell extends StackPane {

	private ClueFXBridge clueFx;
	private MosaicPane mosaicPane;

	private Label x;
	private Label number;

	private BooleanProperty showFill;
	private BooleanProperty showNumber;
	private BooleanProperty showPixel;

	private ObjectProperty<Elevation> elevation;
	private BooleanProperty selected;
	private BooleanProperty highlighted;
	private BooleanProperty error;

	private ObjectProperty<EditorType> editor;

	private static final PseudoClass filled_pc = PseudoClass.getPseudoClass("filled");
	private static final PseudoClass xed_pc = PseudoClass.getPseudoClass("x-ed");
	private static final PseudoClass isPixel_pc = PseudoClass.getPseudoClass("is-pixel");

	private static final PseudoClass raised_pc = PseudoClass.getPseudoClass("raised");
	private static final PseudoClass lowered_pc = PseudoClass.getPseudoClass("lowered");
	private static final PseudoClass selected_pc = PseudoClass.getPseudoClass("selected");
	private static final PseudoClass highlighted_pc = PseudoClass.getPseudoClass("highlighted");
	private static final PseudoClass error_pc = PseudoClass.getPseudoClass("error");

	private static final PseudoClass pixelEditor_pc = PseudoClass.getPseudoClass("pixel-editor");
	private static final PseudoClass clueEditor_pc = PseudoClass.getPseudoClass("clue-editor");
	private static final PseudoClass fillEditor_pc = PseudoClass.getPseudoClass("fill-editor");

	private BooleanProperty hasFilledPseudoClass;
	private BooleanProperty hasXPseudoClass;
	private BooleanProperty hasIsPixelPseudoClass;

	private ObjectBinding<EditorType> focusingEditor;

	public ClueCell(ClueFXBridge clueFx, MosaicPane pane) {
		this.clueFx = clueFx;
		mosaicPane = pane;

		showFill = new SimpleBooleanProperty(this, "showFill", true);
		showNumber = new SimpleBooleanProperty(this, "showNumber", true);
		showPixel = new SimpleBooleanProperty(this, "showPixel", false);

		elevation = new SimpleObjectProperty<Elevation>(this, "elevation", NORMAL) {
			@Override
			protected void invalidated() {
				Elevation elev = get();

				pseudoClassStateChanged(raised_pc, elev == RAISED);
				pseudoClassStateChanged(lowered_pc, elev == LOWERED);
			}
		};

		selected = new SimpleBooleanProperty(this, "selected", false) {
			@Override
			protected void invalidated() {
				pseudoClassStateChanged(selected_pc, get());
			}
		};

		highlighted = new SimpleBooleanProperty(this, "highlighted", false) {
			@Override
			protected void invalidated() {
				pseudoClassStateChanged(highlighted_pc, get());
			}
		};

		error = new SimpleBooleanProperty(this, "error", false) {
			@Override
			protected void invalidated() {
				pseudoClassStateChanged(error_pc, get());
			}
		};

		hasFilledPseudoClass = new SimpleBooleanProperty() {
			@Override
			protected void invalidated() {
				pseudoClassStateChanged(filled_pc, get());
			}
		};

		hasXPseudoClass = new SimpleBooleanProperty() {
			@Override
			protected void invalidated() {
				pseudoClassStateChanged(xed_pc, get());
			}
		};

		hasIsPixelPseudoClass = new SimpleBooleanProperty() {
			@Override
			protected void invalidated() {
				pseudoClassStateChanged(isPixel_pc, get());
			}
		};

		hasFilledPseudoClass.bind(clueFx.fillProperty().isEqualTo(FILLED).and(showFill));
		hasXPseudoClass.bind(clueFx.fillProperty().isEqualTo(X).and(showFill));
		hasIsPixelPseudoClass.bind(clueFx.isPixelProperty().and(showPixel));

		editor = new SimpleObjectProperty<>(this, "editor", NONE);

		focusingEditor = Bindings.createObjectBinding(() -> isFocused() ? getEditor() : NONE, focusedProperty(),
				editor);
		focusingEditor.addListener(obs -> {
			EditorType type = focusingEditor.get();

			pseudoClassStateChanged(clueEditor_pc, type == CLUE);
			pseudoClassStateChanged(pixelEditor_pc, type == PIXEL);
			pseudoClassStateChanged(fillEditor_pc, type == FILL);

		});

		focusTraversableProperty().bind(editor.isNotEqualTo(NONE));

		getStyleClass().add("clue-pane");

		x = new Label("X");
		x.setMinSize(0, 0);
		x.getStyleClass().add("x");
		x.visibleProperty().bind(hasXPseudoClass);

		number = new Label();
		number.setMinSize(0, 0);
		number.getStyleClass().add("number");
		number.textProperty().bind(Bindings.when(clueFx.clueProperty().isEqualTo(-1)).then(" ")
				.otherwise(clueFx.clueProperty().asString()));
		number.visibleProperty().bind(showNumber);

		getChildren().addAll(x, number);

		setOnMousePressed(this::mousePressed);
		setOnMouseReleased(this::mouseReleased);
		setOnMouseExited(this::mouseExited);
		setOnScroll(this::scrolled);
		setOnKeyTyped(this::keyTyped);

		setOnKeyPressed(Event::consume); // restrict scroll-pane to move it
		setOnContextMenuRequested(Event::consume); // prevent context menu
	}

	public ClueFXBridge getClueFXBridge() {
		return clueFx;
	}

	public final boolean isShowFill() {
		return showFillProperty().get();
	}

	public final void setShowFill(boolean flag) {
		showFillProperty().set(flag);
	}

	public BooleanProperty showFillProperty() {
		return showFill;
	}

	public final boolean isShowNumber() {
		return showNumberProperty().get();
	}

	public final void setShowNumber(boolean flag) {
		showNumberProperty().set(flag);
	}

	public BooleanProperty showNumberProperty() {
		return showNumber;
	}

	public final boolean isShowPixel() {
		return showPixelProperty().get();
	}

	public final void setShowPixel(boolean flag) {
		showPixelProperty().set(flag);
	}

	public BooleanProperty showPixelProperty() {
		return showPixel;
	}

	public final Elevation getElevation() {
		return elevationProperty().get();
	}

	public final void setElevation(Elevation elev) {
		elevationProperty().set(elev);
	}

	public ObjectProperty<Elevation> elevationProperty() {
		return elevation;
	}

	public final boolean isSelected() {
		return selectedProperty().get();
	}

	public final void setSelected(boolean flag) {
		selectedProperty().set(flag);
	}

	public BooleanProperty selectedProperty() {
		return selected;
	}

	public final boolean isHighlighted() {
		return highlightedProperty().get();
	}

	public final void setHighlighted(boolean flag) {
		highlightedProperty().set(flag);
	}

	public BooleanProperty highlightedProperty() {
		return highlighted;
	}

	public final boolean isError() {
		return errorProperty().get();
	}

	public final void setError(boolean flag) {
		errorProperty().set(flag);
	}

	public BooleanProperty errorProperty() {
		return error;
	}

	public final EditorType getEditor() {
		return editorProperty().get();
	}

	public final void setEditor(EditorType type) {
		editorProperty().set(type);
	}

	public ObjectProperty<EditorType> editorProperty() {
		return editor;
	}

	private boolean primaryDown;
	private boolean secondaryDown;
	private boolean exited;

	private void mousePressed(MouseEvent evt) {
		if (getEditor() == NONE)
			return;
		if (!isFocused())
			Platform.runLater(() -> requestFocus());

		primaryDown = evt.isPrimaryButtonDown();
		secondaryDown = evt.isSecondaryButtonDown();
		exited = false;

		evt.consume(); // restrict scroll-pane to grab focus
	}

	private void mouseReleased(MouseEvent evt) {
		if (!isFocused() || exited)
			return;

		if (getEditor() == FILL) {
			if (primaryDown) {
				if (clueFx.getFill() == FILLED)
					clueFx.setFill(EMPTY);
				else
					clueFx.setFill(FILLED);
			}
			if (secondaryDown) {
				if (clueFx.getFill() == X)
					clueFx.setFill(EMPTY);
				else
					clueFx.setFill(X);
			}
		} else if (getEditor() == PIXEL) {
			clueFx.setIsPixel(!clueFx.isPixel());
		} else if (getEditor() == CLUE) {
			if (primaryDown) {
				int linear = mosaicPane.getClueCells().indexOf(this);
				int x = mosaicPane.getClueCells().toX(linear);
				int y = mosaicPane.getClueCells().toY(linear);

				mosaicPane.getMosaic().putClue(x, y);
			} else if (secondaryDown)
				clueFx.setClue(-1);
		}
	}

	private void mouseExited(MouseEvent evt) {
		exited = true;
	}

	private double scrollDelta;

	private void scrolled(ScrollEvent evt) {
		if (getEditor() != CLUE)
			return;
		if (!isFocused())
			return;

		scrollDelta += (evt.getTextDeltaY() / 3) * 2; // sensitivity

		scrollDelta = Math.max(scrollDelta, -1);
		scrollDelta = Math.min(scrollDelta, 1);

		int newClue = clueFx.getClue() + (int) scrollDelta;
		if (newClue != clueFx.getClue())
			scrollDelta = 0;

		newClue = Math.min(newClue, 9);
		newClue = Math.max(newClue, 0);

		clueFx.setClue(newClue);

		evt.consume(); // restrict scroll-pane to move it
	}

	private void keyTyped(KeyEvent evt) {
		char key = evt.getCharacter().charAt(0);

		if (getEditor() == CLUE) {
			if (Character.isDigit(key)) {
				clueFx.setClue(key - '0');
			}
		} else if (getEditor() == FILL) {
			if (key == ' ') {
				if (clueFx.getFill() == EMPTY)
					clueFx.setFill(FILLED);
				else if (clueFx.getFill() == FILLED)
					clueFx.setFill(X);
				else
					clueFx.setFill(EMPTY);
			}
		} else if (getEditor() == PIXEL) {
			if (key == ' ')
				clueFx.setIsPixel(!clueFx.isPixel());
		}
	}
}
