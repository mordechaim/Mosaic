package com.stackexchange.puzzling.user.mordechai.mosaic.fx;

import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class Center extends VBox {

	private static final Global global = Global.getInstance();

	private FillTransition fader;

	public Center() {

		Group group = new Group();
		Slider scale = new Slider(0.2, 1, 1);
		scale.setPadding(new Insets(20));
		scale.setTooltip(new Tooltip("Mosaic size"));

		Description description = new Description();
		description.setPadding(new Insets(10));

		StackPane stack = new StackPane(description);

		global.mosaicPaneProperty().addListener((obs, ov, nv) -> {
			stack.getChildren().clear();
			group.getChildren().clear();
			if (ov != null) {
				ov.scaleXProperty().unbind();
				ov.scaleYProperty().unbind();
			}
			if (nv != null) {
				nv.scaleXProperty().bind(scale.valueProperty());
				nv.scaleYProperty().bind(scale.valueProperty());
				group.getChildren().add(nv);
				stack.getChildren().add(group);

				if (!getChildren().contains(scale)) {
					getChildren().add(scale);
				}
			} else {
				stack.getChildren().add(description);
				getChildren().remove(scale);
			}
		});

		ScrollPane scroll = new ScrollPane(stack);
		scroll.setFitToWidth(true);
		scroll.setFitToHeight(true);

		setVgrow(scroll, Priority.ALWAYS);
		setAlignment(Pos.CENTER);

		getChildren().add(scroll);

		fader = new FillTransition(Duration.seconds(2));
		fader.setToValue(Color.BLACK);
		fader.setAutoReverse(true);
		fader.setCycleCount(Animation.INDEFINITE);

		global.mosaicPaneProperty().addListener((obs, ov, nv) -> {
			if (nv != null) {
				boolean running = fader.getStatus() == Animation.Status.RUNNING;

				fader.jumpTo(Duration.ZERO);
				fader.stop();
				fader.setShape(nv.background());

				if (running) {
					fader.playFromStart();
				}
			}
		});
		global.offCanvasOperatingProperty().addListener((obs, ov, nv) -> {
			if (nv && global.getMosaicPane() != null) {
				fader.playFromStart();
			} else {
				fader.jumpTo(Duration.ZERO);
				fader.stop();
			}
		});

	}

}
