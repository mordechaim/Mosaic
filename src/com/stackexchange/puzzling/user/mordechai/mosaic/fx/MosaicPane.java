package com.stackexchange.puzzling.user.mordechai.mosaic.fx;

import static com.stackexchange.puzzling.user.mordechai.mosaic.fx.EditorType.*;

import com.stackexchange.puzzling.user.mordechai.grid.Grid;
import com.stackexchange.puzzling.user.mordechai.grid.GridIterator;
import com.stackexchange.puzzling.user.mordechai.mosaic.Clue;
import com.stackexchange.puzzling.user.mordechai.mosaic.Mosaic;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class MosaicPane extends StackPane {

	private Mosaic mosaic;
	private Grid<ClueCell> cells;
	private ObjectProperty<EditorType> editor;

	private Rectangle background;

	public static final int MAX_SIZE = 100;

	public MosaicPane(Mosaic mosaic, boolean convert) {
		if (mosaic.width() > MAX_SIZE || mosaic.height() > MAX_SIZE)
			throw new IllegalArgumentException("Mosaic size is larger than" + MAX_SIZE);

		this.mosaic = mosaic;
		cells = new Grid<>(mosaic.width(), mosaic.height());

		setAlignment(Pos.CENTER);

		editor = new SimpleObjectProperty<>(this, "editor", NONE);

		if (convert) {
			mosaic.grid().fill((x, y, old) -> new ClueFXBridge(old));
		}

		GridPane grid = new GridPane();
		try {
			GridIterator<Clue> iterator = mosaic.iterator();
			while (iterator.hasNext()) {
				ClueFXBridge cluefx = (ClueFXBridge) iterator.next();

				ClueCell clueCell = new ClueCell(cluefx, this);
				clueCell.editorProperty().bind(editor);
				clueCell.showPixelProperty().bind(editor.isEqualTo(PIXEL).or(editor.isEqualTo(CLUE)));
				clueCell.showFillProperty().bind(clueCell.showPixelProperty().not());
				clueCell.showNumberProperty().bind(editor.isNotEqualTo(PIXEL));

				grid.add(clueCell, iterator.x(), iterator.y());
				cells.set(iterator.x(), iterator.y(), clueCell);
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Mosaic must have grid of ClueFXBridge.");
		}

		background = new Rectangle();
		background.widthProperty().bind(grid.widthProperty());
		background.heightProperty().bind(grid.heightProperty());

		grid.getStyleClass().add("grid");
		background.getStyleClass().add("background");

		getChildren().addAll(background, new Group(grid));
		getStyleClass().add("mosaic-pane");
		getStylesheets().add(getClass().getResource("resources/css/mosaic-pane-defaults.css").toExternalForm());
	}

	public MosaicPane(Mosaic mosaic) {
		this(mosaic, false);
	}

	public ClueCell getAt(int x, int y) {
		return cells.get(x, y);
	}

	private Grid<ClueCell> clone;

	public Grid<ClueCell> getClueCells() {
		if (clone == null)
			clone = new Grid<ClueCell>(cells);

		return clone;
	}

	public Mosaic getMosaic() {
		return mosaic;
	}

	public EditorType getEditor() {
		return editor.get();
	}

	public void setEditor(EditorType type) {
		editor.set(type);
	}

	public ObjectProperty<EditorType> editorProperty() {
		return editor;
	}

	Rectangle background() {
		return background;
	}

}
