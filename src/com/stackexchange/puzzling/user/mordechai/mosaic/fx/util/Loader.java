package com.stackexchange.puzzling.user.mordechai.mosaic.fx.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;

import com.stackexchange.puzzling.user.mordechai.mosaic.Mosaic;
import com.stackexchange.puzzling.user.mordechai.mosaic.fx.EditorType;
import com.stackexchange.puzzling.user.mordechai.mosaic.fx.Global;
import com.stackexchange.puzzling.user.mordechai.mosaic.fx.MosaicPane;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser.ExtensionFilter;

public class Loader { // FIXME use threads for loading and set off-canvas-processing

	private Loader() {
	}

	private static List<ExtensionFilter> filters;
	private static List<String> textualFormats;
	private static List<String> imageFormats;

	public static List<ExtensionFilter> extensionFilters() {
		return filters;
	}

	public static List<String> imageFormats() {
		return imageFormats;
	}

	public static List<String> textualFormats() {
		return textualFormats;
	}

	static {
		filters = new ArrayList<>();
		imageFormats = Collections.unmodifiableList(Arrays.asList("*.png", "*.jpg", "*.jpeg", "*.gif"));
		textualFormats = Collections.unmodifiableList(Arrays.asList("*.csv", "*.txt"));

		filters.add(new ExtensionFilter("Comma Separated Values", textualFormats));
		filters.add(new ExtensionFilter("Image", imageFormats));
		filters.add(new ExtensionFilter("All Files", "*.*"));

		filters = Collections.unmodifiableList(filters);
	}
	private static Global global = Global.getInstance();

	public static boolean isSupportedFileExtension(File f) {
		String name = f.getName().toLowerCase();
		String format = "*" + name.substring(name.lastIndexOf("."));
		return imageFormats.contains(format) || textualFormats.contains(format);
	}

	public static boolean isTextualFile(File f) {
		try {
			return !isBinaryFile(f);
		} catch (IOException e) {
			return false;
		}
	}

	/*
	 * https://stackoverflow.com/a/13533390
	 */
	private static boolean isBinaryFile(File f) throws FileNotFoundException, IOException {
		FileInputStream in = new FileInputStream(f);
		int size = in.available();
		if (size > 1024)
			size = 1024;
		byte[] data = new byte[size];
		in.read(data);
		in.close();

		int ascii = 0;
		int other = 0;

		for (int i = 0; i < data.length; i++) {
			byte b = data[i];
			if (b < 0x09)
				return true;

			if (b == 0x09 || b == 0x0A || b == 0x0C || b == 0x0D)
				ascii++;
			else if (b >= 0x20 && b <= 0x7E)
				ascii++;
			else
				other++;
		}

		if (other == 0)
			return false;

		return 100 * other / (ascii + other) > 95;
	}

	private static final String DONT_SHOW = "dontShowOverwrite";
	private static boolean showedAlready = false;

	public static boolean overwrite() {

		if (!showedAlready && global.getMosaicPane() != null) {

			Preferences prefs = Preferences.userNodeForPackage(Loader.class);
			boolean bool = prefs.getBoolean(DONT_SHOW, false);

			if (!bool) {
				return showedAlready = Util.createAlertWithOptOut(AlertType.CONFIRMATION, "Confirm Load",
						"Do you want to load the new data?", "Current work will be lost, you may want to export first",
						"Do not ask again", param -> {
							prefs.putBoolean(DONT_SHOW, param);

						}, ButtonType.YES, ButtonType.NO).filter(t -> t == ButtonType.YES).isPresent();
			}

		}

		return true;
	}

	public static void load(File file) {
		if (!file.isFile()) {
			Alert error = new Alert(AlertType.ERROR, "Could not load " + file.getName());
			error.showAndWait();
			return;
		}

		String name = file.getName().toLowerCase();
		String format = "*" + name.substring(name.lastIndexOf("."));

		if (imageFormats.contains(format)) {
			loadImage(file);
		} else if (textualFormats.contains(format) && isTextualFile(file)) {
			loadString(file);
		} else {
			Alert error = new Alert(AlertType.ERROR, "Could not load " + file.getName());
			error.showAndWait();
		}
	}

	public static void save(File file) {
		String name = file.getName().toLowerCase();
		String format = "*" + name.substring(name.lastIndexOf("."));
		if (imageFormats.contains(format)) {
			saveImage(file);
		} else {
			saveString(file);
		}
	}

	public static void newMosaic() {
		Spinner<Integer> width = new Spinner<>(1, MosaicPane.MAX_SIZE, 10);
		Spinner<Integer> height = new Spinner<>(1, MosaicPane.MAX_SIZE, 10);
		width.setEditable(true);
		height.setEditable(true);

		// hack for committing on focus lose
		TextFormatter<Integer> widthFmt = new TextFormatter<>(width.getValueFactory().getConverter(), width.getValue());
		width.getEditor().setTextFormatter(widthFmt);
		width.getValueFactory().valueProperty().bindBidirectional(widthFmt.valueProperty());

		TextFormatter<Integer> heightFmt = new TextFormatter<>(height.getValueFactory().getConverter(),
				height.getValue());
		height.getEditor().setTextFormatter(heightFmt);
		height.getValueFactory().valueProperty().bindBidirectional(heightFmt.valueProperty());

		Label widthLabel = new Label(" Width:");
		Label heightLabel = new Label("Height:");

		HBox widthBox = new HBox(10);
		HBox heightBox = new HBox(10);

		widthBox.setAlignment(Pos.CENTER);
		heightBox.setAlignment(Pos.CENTER);

		widthBox.getChildren().addAll(widthLabel, width);
		heightBox.getChildren().addAll(heightLabel, height);

		VBox boxes = new VBox(widthBox, heightBox);
		boxes.setSpacing(10);
		boxes.setPadding(new Insets(15));

		Util.createDialog(boxes, "Create New Mosaic...", () -> {
			Mosaic m = new Mosaic(width.getValue(), height.getValue());
			MosaicPane mp = new MosaicPane(m, true);
			global.setMosaicPane(mp);
		}, ButtonType.OK, ButtonType.CANCEL);
	}

	public static void saveString(File file) {
		Text text = new Text("Select the attributes to be saved");

		CheckBox fill = new CheckBox("Fill");
		fill.setSelected(true);
		CheckBox clue = new CheckBox("Clues");
		clue.setSelected(true);
		CheckBox image = new CheckBox("Underlying Image");
		image.setSelected(true);

		VBox box = new VBox(fill, clue, image);
		box.setSpacing(10);
		box.setPadding(new Insets(10));
		box.setAlignment(Pos.CENTER_LEFT);

		VBox vbox = new VBox(10);
		vbox.setAlignment(Pos.CENTER);

		vbox.getChildren().addAll(text, box);
		vbox.setPadding(new Insets(15));

		Util.createDialog(vbox, "Export Attributes...", () -> {
			try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
				out.print(global.getMosaicPane().getMosaic().toCSV(fill.isSelected(), clue.isSelected(),
						image.isSelected()));
			} catch (IOException e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText("Cannot save to file  " + file.getName());
				alert.setTitle("Error");
				alert.showAndWait();
			}
		}, ButtonType.OK, ButtonType.CANCEL);

	}

	public static void saveImage(File file) {
		try {
			Image image = global.getMosaicPane().snapshot(new SnapshotParameters(), null);
			String name = file.getName();
			if (!ImageIO.write(SwingFXUtils.fromFXImage(image, null), name.substring(name.lastIndexOf(".") + 1), file))
				throw new IOException();
		} catch (IOException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setHeaderText("Cannot save image to file  " + file.getName());
			alert.setTitle("Error");
			alert.showAndWait();
		}
	}

	public static void loadString(File f) {
		Mosaic mosaic = null;
		try {
			mosaic = Mosaic.loadString(f);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		global.setMosaicPane(new MosaicPane(mosaic, true));
	}
	
	public static void loadString(InputStream in) {
		Mosaic mosaic = null;
		try {
			mosaic = Mosaic.loadString(in);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		global.setMosaicPane(new MosaicPane(mosaic, true));
	}

	public static void loadString(String str) {
		Mosaic mosaic = Mosaic.fromString(str);

		global.setMosaicPane(new MosaicPane(mosaic, true));
	}

	public static void loadImage(File f) {
		Image image = new Image(f.getAbsolutePath());

		if (image.isError()) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setHeaderText("Cannot open image " + f.getName());
			alert.setTitle("Error");
			alert.showAndWait();
			return;
		}

		loadImage(image);
	}
	
	public static void loadImage(InputStream in) {
		Image image = new Image(in);

		if (image.isError()) {
			throw new RuntimeException(image.getException());
		}

		loadImage(image);
	}

	public static void loadImage(Image image) {
		Spinner<Integer> width = new Spinner<>(1, 100, Math.min(100, (int) image.getWidth()));
		Spinner<Integer> height = new Spinner<>(1, 100, Math.min(100, (int) image.getHeight()));
		width.setEditable(true);
		height.setEditable(true);

		ImageView imageView = new ImageView(resample(blackAndWhite(image, width.getValue(), height.getValue()), 10));
		imageView.setSmooth(false);
		imageView.setFitWidth(MosaicPane.MAX_SIZE);
		imageView.setFitHeight(MosaicPane.MAX_SIZE);

		// hack for committing on focus lose
		TextFormatter<Integer> widthFmt = new TextFormatter<>(width.getValueFactory().getConverter(), width.getValue());
		width.getEditor().setTextFormatter(widthFmt);
		width.getValueFactory().valueProperty().bindBidirectional(widthFmt.valueProperty());

		TextFormatter<Integer> heightFmt = new TextFormatter<>(height.getValueFactory().getConverter(),
				height.getValue());
		height.getEditor().setTextFormatter(heightFmt);
		height.getValueFactory().valueProperty().bindBidirectional(heightFmt.valueProperty());

		width.getValueFactory().valueProperty().addListener((obs, ov, nv) -> imageView
				.setImage(resample(blackAndWhite(image, width.getValue(), height.getValue()), 10)));
		height.getValueFactory().valueProperty().addListener((obs, ov, nv) -> imageView
				.setImage(resample(blackAndWhite(image, width.getValue(), height.getValue()), 10)));

		Label widthLabel = new Label(" Width:");
		Label heightLabel = new Label("Height:");

		HBox widthBox = new HBox(10);
		HBox heightBox = new HBox(10);

		widthBox.setAlignment(Pos.CENTER);
		heightBox.setAlignment(Pos.CENTER);

		widthBox.getChildren().addAll(widthLabel, width);
		heightBox.getChildren().addAll(heightLabel, height);

		VBox vbox = new VBox(10);
		vbox.setAlignment(Pos.CENTER);

		vbox.getChildren().addAll(imageView, widthBox, heightBox);
		vbox.setPadding(new Insets(15));

		Util.createDialog(vbox, "Import Image...", () -> {
			Mosaic m = Mosaic.loadScaledImage(SwingFXUtils.fromFXImage(image, null), width.getValue(),
					height.getValue());
			MosaicPane mp = new MosaicPane(m, true);
			mp.setEditor(EditorType.PIXEL);

			global.setMosaicPane(mp);
		}, ButtonType.OK, ButtonType.CANCEL);
	}

	private static Image blackAndWhite(Image image, int width, int height) {

		BufferedImage blackAndWhite = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D g2d = blackAndWhite.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g2d.fillRect(0, 0, width, height);
		g2d.drawImage(SwingFXUtils.fromFXImage(image, null), 0, 0, width, height, null);
		g2d.dispose();

		return SwingFXUtils.toFXImage(blackAndWhite, null);
	}

	/*
	 * https://stackoverflow.com/a/16092631
	 */
	private static Image resample(Image input, int scaleFactor) {
		final int W = (int) input.getWidth();
		final int H = (int) input.getHeight();
		final int S = scaleFactor;

		WritableImage output = new WritableImage(W * S, H * S);

		PixelReader reader = input.getPixelReader();
		PixelWriter writer = output.getPixelWriter();

		for (int y = 0; y < H; y++) {
			for (int x = 0; x < W; x++) {
				final int argb = reader.getArgb(x, y);
				for (int dy = 0; dy < S; dy++) {
					for (int dx = 0; dx < S; dx++) {
						writer.setArgb(x * S + dx, y * S + dy, argb);
					}
				}
			}
		}

		return output;
	}

}
