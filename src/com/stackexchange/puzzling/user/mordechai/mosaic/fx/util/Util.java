package com.stackexchange.puzzling.user.mordechai.mosaic.fx.util;

import java.util.function.Consumer;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Alert.AlertType;

public class Util {

	private Util() {
	}

	/*
	 * https://stackoverflow.com/a/36949596
	 */
	public static Alert createAlertWithOptOut(AlertType type, String title, String headerText, String message,
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

		return alert;
	}
}
