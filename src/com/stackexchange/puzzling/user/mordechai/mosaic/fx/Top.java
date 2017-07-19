package com.stackexchange.puzzling.user.mordechai.mosaic.fx;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.stackexchange.puzzling.user.mordechai.mosaic.fx.util.Loader;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

public class Top extends BorderPane {

	private Button newMosaic;
	private Button importMosaic;
	private Button exportMosaic;
	private MenuButton presets;
	private Button clear;

	private File importedFile;
	private File exportedFile;

	private static final Global global = Global.getInstance();

	public Top() {
		HBox hbox = new HBox(10);

		newMosaic = new Button("New");
		importMosaic = new Button("Import");
		exportMosaic = new Button("Export");
		presets = new MenuButton("Presets");
		clear = new Button("x");

		newMosaic.setTooltip(new Tooltip("Create new mosaic"));
		importMosaic.setTooltip(new Tooltip("Import from file (csv/image)"));
		exportMosaic.setTooltip(new Tooltip("Export to file (csv/image)"));
		presets.setTooltip(new Tooltip("Load preset mosaic"));
		clear.setTooltip(new Tooltip("Clear active mosaic"));

		List<MenuItem> items = new ArrayList<MenuItem>();

		for (int i = 1; i <= 12; i++) {
			InputStream in = getClass().getResourceAsStream("resources/presets/Preset " + i + ".csv");
			String name = "Preset " + i;
			MenuItem item = new MenuItem(name);
			items.add(item);
			item.setOnAction(evt -> {
				Loader.loadString(in);
			});
		}

		Collections.sort(items,
				Comparator.comparingInt(item -> Integer.parseInt(item.getText().replaceAll("\\D", ""))));
		presets.getItems().addAll(items);

		newMosaic.setFocusTraversable(false);
		importMosaic.setFocusTraversable(false);
		exportMosaic.setFocusTraversable(false);
		presets.setFocusTraversable(false);
		clear.setFocusTraversable(false);

		exportMosaic.disableProperty().bind(global.mosaicPaneProperty().isNull());
		clear.disableProperty().bind(exportMosaic.disableProperty());

		newMosaic.setOnAction(evt -> Loader.newMosaic());
		importMosaic.setOnAction(evt -> importMosaic());
		exportMosaic.setOnAction(evt -> exportMosaic());
		clear.setOnAction(evt -> global.setMosaicPane(null));

		hbox.setAlignment(Pos.CENTER);
		setPadding(new Insets(10));

		disableProperty().bind(global.autoSolvingProperty().or(global.generatingProperty()));
		hbox.getChildren().addAll(newMosaic, importMosaic, exportMosaic, presets);

		setCenter(hbox);
		setRight(clear);
	}

	public void importMosaic() {
		FileChooser chooser = new FileChooser();

		chooser.getExtensionFilters().addAll(Loader.extensionFilters());
		chooser.setTitle("Import File...");
		chooser.setInitialDirectory(
				importedFile == null ? new File(System.getProperty("user.home")) : importedFile.getParentFile());

		importedFile = chooser.showOpenDialog(importMosaic.getScene().getWindow());

		if (importedFile != null) {
			Loader.load(importedFile);
		}
	}

	public void exportMosaic() {

		FileChooser chooser = new FileChooser();

		chooser.getExtensionFilters().addAll(Loader.extensionFilters());
		chooser.setTitle("Export File...");
		chooser.setInitialDirectory(exportedFile == null
				? importedFile == null ? new File(System.getProperty("user.home")) : importedFile.getParentFile()
				: exportedFile.getParentFile());

		exportedFile = chooser.showSaveDialog(exportMosaic.getScene().getWindow());

		if (exportedFile != null) {
			Loader.save(exportedFile);
		}
	}

}
