package com.stackexchange.puzzling.user.mordechai.mosaic.fx.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Separator;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

public class Util {

	private Util() {
	}

	public static Optional<ButtonType> createDialog(Node content, String title, ButtonType... buttonTypes) {
		
		Stage dialog = new Stage();
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.setAlwaysOnTop(true);
		dialog.setResizable(false);
		dialog.setTitle(title);

		List<Button> buttons = new ArrayList<>(buttonTypes.length);
		ButtonType[] response = new ButtonType[1];

		for (ButtonType type : buttonTypes) {
			Button b = new Button(type.getText());
			b.setDefaultButton(type.getButtonData().isDefaultButton());
			b.setCancelButton(type.getButtonData().isCancelButton());

			b.setOnAction(evt -> {
				response[0] = type;
				dialog.close();
			});

			buttons.add(b);
		}

		HBox buttonBox = new HBox();
		buttonBox.getChildren().addAll(buttons);
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.setSpacing(10);
		buttonBox.setPadding(new Insets(10, 0, 10, 0));

		VBox vbox = new VBox();
		vbox.getChildren().addAll(content, new Separator(), buttonBox);

		Scene scene = new Scene(new StackPane(vbox));
		dialog.setScene(scene);

		if (buttons.size() > 0) {
			dialog.setOnShown(evt -> buttons.get(0).requestFocus());
		}

		dialog.showAndWait();

		return Optional.ofNullable(response[0]);
	}

	/*
	 * https://stackoverflow.com/a/36949596
	 */
	public static Optional<ButtonType> createAlertWithOptOut(AlertType type, String title, String headerText,
			String message,

			String optOutMessage, Consumer<Boolean> optOutAction, ButtonType... buttonTypes) {
		Alert alert = new Alert(type);

		alert.getDialogPane().applyCss();
		Node graphic = alert.getDialogPane().getGraphic();

		alert.setDialogPane(new DialogPane() {
			@Override
			protected Node createDetailsButton() {
				CheckBox optOut = new CheckBox();
				optOut.setText(optOutMessage);
				optOut.setOnAction(e -> optOutAction.accept(optOut.isSelected()));
				return optOut;
			}
		});
		alert.getDialogPane().getButtonTypes().addAll(buttonTypes);
		alert.getDialogPane().setContentText(message);
		// Fool the dialog into thinking there is some expandable content
		// a Group won't take up any space if it has no children
		alert.getDialogPane().setExpandableContent(new Group());
		alert.getDialogPane().setExpanded(true);
		// Reset the dialog graphic using the default style
		alert.getDialogPane().setGraphic(graphic);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setResizable(false);

		return alert.showAndWait();
	}
}
