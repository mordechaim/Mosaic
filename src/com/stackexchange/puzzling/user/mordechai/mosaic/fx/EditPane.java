package com.stackexchange.puzzling.user.mordechai.mosaic.fx;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static com.stackexchange.puzzling.user.mordechai.mosaic.fx.EditorType.*;

import com.stackexchange.puzzling.user.mordechai.mosaic.Clue;
import com.stackexchange.puzzling.user.mordechai.mosaic.Fill;

public class EditPane extends VBox {

	private ToggleButton fill;
	private ToggleButton clue;
	private ToggleButton image;

	private Button clear;
	private Button fillAll;

	private ObjectBinding<EditorType> selectedCollection;
	private ObjectBinding<EditorType> mosaicPaneEditorValue;

	private static final Global global = Global.getInstance();

	public EditPane() {
		super(5);

		fill = new ToggleButton("Fill");
		clue = new ToggleButton("Clue");
		image = new ToggleButton("Image");

		clear = new Button("Clear");
		fillAll = new Button("Fill All");

		fill.setTooltip(new Tooltip("Edit cell fill"));
		clue.setTooltip(new Tooltip("Edit cell clues"));
		image.setTooltip(new Tooltip("Edit underlying image"));

		clear.setTooltip(new Tooltip("Clear the mosaic according\nto the current selected editor"));
		fillAll.setTooltip(new Tooltip("Fill the mosaic according\nto the current selected editor"));

		fill.setFocusTraversable(false);
		clue.setFocusTraversable(false);
		image.setFocusTraversable(false);

		clear.setFocusTraversable(false);
		fillAll.setFocusTraversable(false);

		ToggleGroup group = new ToggleGroup();
		fill.setToggleGroup(group);
		clue.setToggleGroup(group);
		image.setToggleGroup(group);

		clear.setOnAction(evt -> {
			MosaicPane mp = global.getMosaicPane();
			if (mp.getEditor() == FILL)
				global.getMosaicPane().getMosaic().forEach(clue -> clue.setFill(Fill.EMPTY));
			else if (mp.getEditor() == CLUE)
				global.getMosaicPane().getMosaic().forEach(clue -> clue.setClue(-1));
			else if (mp.getEditor() == PIXEL)
				global.getMosaicPane().getMosaic().forEach(clue -> clue.setIsPixel(false));
		});

		fillAll.setOnAction(evt -> {
			MosaicPane mp = global.getMosaicPane();
			if (mp.getEditor() == FILL) {
				if (mp.getMosaic().grid().count(clue -> clue.getFill() == Fill.FILLED) == mp.getMosaic().length())
					mp.getMosaic().grid().forEach(clue -> clue.setFill(Fill.X));
				else
					mp.getMosaic().grid().forEach(clue -> clue.setFill(Fill.FILLED));
			} else if (mp.getEditor() == CLUE) {
				mp.getMosaic().fillClues();
			} else if (mp.getEditor() == PIXEL) {
				int filled = 0;
				int pixeled = 0;
				int joint = 0;

				for (Clue clue : mp.getMosaic()) {
					if (clue.getFill() == Fill.FILLED) {
						filled++;
					}
					if (clue.isPixel()) {
						pixeled++;
					}
					if (clue.getFill() == Fill.FILLED && clue.isPixel()) {
						joint++;
					}
				}

				if (filled == 0 || (filled ==  pixeled && pixeled == joint)) {
					mp.getMosaic().grid().forEach(clue -> clue.setIsPixel(true));
				} else {
					mp.getMosaic().grid().forEach(clue -> clue.setIsPixel(clue.getFill() == Fill.FILLED));
				}
				
			}
		});

		selectedCollection = Bindings.createObjectBinding(() -> {
			if (fill.isSelected())
				return FILL;
			else if (clue.isSelected())
				return CLUE;
			else if (image.isSelected())
				return PIXEL;
			else
				return NONE;

		}, fill.selectedProperty(), clue.selectedProperty(), image.selectedProperty());

		selectedCollection.addListener((obs, ov, nv) -> {
			if (global.getMosaicPane() != null)
				global.getMosaicPane().setEditor(selectedCollection.getValue());
		});

		clear.disableProperty().bind(selectedCollection.isEqualTo(NONE));
		fillAll.disableProperty().bind(selectedCollection.isEqualTo(NONE));

		mosaicPaneEditorValue = Bindings.select(global.mosaicPaneProperty(), "editor");
		mosaicPaneEditorValue.addListener((obs, ov, nv) -> {
			fill.setSelected(nv == FILL);
			clue.setSelected(nv == CLUE);
			image.setSelected(nv == PIXEL);
		});

		setAlignment(Pos.TOP_CENTER);

		HBox editors = new HBox(fill, clue, image);
		editors.setAlignment(Pos.CENTER);

		HBox fillers = new HBox(clear, fillAll);
		fillers.setSpacing(5);
		fillers.setAlignment(Pos.CENTER);

		getChildren().addAll(editors, fillers);

		getStyleClass().add("edit-pane");
		fill.getStyleClass().add("left");
		clue.getStyleClass().add("center");
		image.getStyleClass().add("right");

		clear.setStyle("-fx-pref-width: 50");
		fillAll.setStyle("-fx-pref-width: 50");

		setPadding(new Insets(10, 50, 0, 50));

		getStylesheets().add(getClass().getResource("resources/css/round-buttons.css").toExternalForm());

	}

	public void liftToggles() {
		fill.setSelected(false);
		clue.setSelected(false);
		image.setSelected(false);
	}
}
