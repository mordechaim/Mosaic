package com.stackexchange.puzzling.user.mordechai.mosaic.fx;

import java.util.Iterator;

import com.stackexchange.puzzling.user.mordechai.mosaic.Clue;
import com.stackexchange.puzzling.user.mordechai.mosaic.Mosaic;

import javafx.beans.binding.Bindings;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GeneratePane extends VBox {

	private Slider level;
	private CheckBox refillClues;
	private ProgressBar progress;
	private Button button;
	private Label percent;

	private Service<Integer> generator;

	private static final Global global = Global.getInstance();

	public GeneratePane() {
		level = new Slider(0, 1, 0);
		level.setPadding(new Insets(5, 20, 5, 20));
		level.disableProperty().bind(global.generatingProperty());
		level.setTooltip(new Tooltip("Puzzle level (higher values\nmay require more time)"));

		refillClues = new CheckBox("Refill Clues");
		refillClues.setSelected(true);
		refillClues.disableProperty().bind(global.generatingProperty());
		refillClues.setFocusTraversable(false);
		refillClues.setTooltip(new Tooltip("Refill the mosaic clues before generating"));

		progress = new ProgressBar();
		progress.prefWidthProperty().bind(level.widthProperty());
		progress.paddingProperty().bind(level.paddingProperty());
		progress.visibleProperty().bind(progress.progressProperty().greaterThan(0));

		initGenerator();

		button = new Button();
		button.textProperty().bind(Bindings.when(generator.runningProperty()).then("Stop").otherwise("Run"));
		button.setFocusTraversable(false);
		button.setOnAction(evt -> {
			if (generator.isRunning()) {
				generator.cancel();
			} else { // TODO create opt-out alert if no underlying image: requires image!
				generator.restart();
			}
		});

		percent = new Label();
		percent.textProperty().bind(generator.messageProperty());
		percent.visibleProperty().bind(progress.visibleProperty());

		HBox hb = new HBox(button, refillClues);
		hb.setSpacing(20);
		hb.setAlignment(Pos.CENTER);

		setAlignment(Pos.CENTER);
		getChildren().addAll(level, hb, percent, progress);
	}

	private Mosaic offCanvas;

	private void initGenerator() {
		generator = new Service<Integer>() {
			@Override
			protected Task<Integer> createTask() {

				if (refillClues.isSelected()) {
					global.getMosaicPane().getMosaic().fillClues();
				}

				Task<Integer> task = new Task<Integer>() {
					@Override
					protected Integer call() throws Exception {
						offCanvas = new Mosaic(global.getMosaicPane().getMosaic().grid().branch(Clue::new));
						Iterator<Integer> iterator = offCanvas.clueGenerator((float) level.getValue(),
								refillClues.isSelected());

						int workDone = 0;
						int job = offCanvas.count(clue -> clue.getClue() >= 0);

						updateMessage("0/" + job);
						updateProgress(0, job);

						while (iterator.hasNext() && !isCancelled()) {
							updateValue(iterator.next());
							workDone++;
							updateProgress(workDone, job);
							updateMessage(workDone + "/" + job);
						}

						updateProgress(0, 0);

						return null;
					}

					@Override
					protected void cancelled() {
						updateMessage("Cancelled...");
					}
				};

				return task;
			}

		};
		
		progress.progressProperty().bind(generator.progressProperty());
		generator.valueProperty().addListener((obs, ov, nv) -> {
			if (ov != null) {
				ClueCell cc = global.getMosaicPane().getClueCells().get(ov);
				cc.setHighlighted(false);
			}
			if (nv != null) {
				ClueCell cc = global.getMosaicPane().getClueCells().get(nv);
				cc.setHighlighted(generator.isRunning());
				cc.getClueFXBridge().setClue(offCanvas.get(nv).getClue());
			}
		});

		global.generatingProperty().bind(generator.runningProperty());
		generator.runningProperty().addListener((obs, ov, nv) -> {
			global.setOffCanvasOperating(nv);
		});

		generator.setOnSucceeded(evt -> global.getMosaicPane().getMosaic().grid()
				.forEach((clue, x, y) -> clue.setClue(offCanvas.get(x, y).getClue())));
		generator.setOnCancelled(
				evt -> global.getMosaicPane().getClueCells().get(generator.getValue()).setHighlighted(false));
	}

}
