package com.stackexchange.puzzling.user.mordechai.mosaic.fx;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.prefs.Preferences;

import com.stackexchange.puzzling.user.mordechai.mosaic.fx.util.Loader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class FXApplication extends Application {

	private static Application app;
	private static Global global = Global.getInstance();

	private static final String WIDTH_KEY = "width";
	private static final String HEIGHT_KEY = "height";
	private static final String MAXIMIZED_KEY = "maximized";
	private static final double WIDTH_DEF_VALUE = 620;
	private static final double HEIGHT_DEF_VALUE = 660;

	@Override
	public void start(Stage primaryStage) {
		app = this;

		primaryStage.getIcons()
				.add(new Image(getClass().getResource("resources/Mosaic.png").toExternalForm()));

		Top top = new Top();
		Center center = new Center();
		Bottom bottom = new Bottom();

		BorderPane root = new BorderPane();
		root.setTop(top);
		root.setCenter(center);

		ContextMenu context = new ContextMenu();

		MenuItem importClipboard = new MenuItem("Import clipboard content");
		Text arrow = new Text("\u21f2");
		arrow.setFont(Font.font(15));
		importClipboard.setGraphic(arrow);
		importClipboard.setOnAction(evt -> importClipboardContent(Clipboard.getSystemClipboard(), false));
		MenuItem exportClues = new MenuItem("Copy to clipboard");
		exportClues.setOnAction(evt -> {
			ClipboardContent c = new ClipboardContent();
			c.putString(global.getMosaicPane().getMosaic().toCSV());
			Clipboard.getSystemClipboard().setContent(c);
		});

		center.setOnContextMenuRequested(evt -> {
			context.getItems().clear();
			if (global.getMosaicPane() != null) {
				context.getItems().add(exportClues);
			}
			if (isValidClipboardContent(Clipboard.getSystemClipboard())) {
				context.getItems().add(importClipboard);
			}

			if (!context.getItems().isEmpty()) {
				context.show(primaryStage, evt.getScreenX(), evt.getScreenY());
			}
		});

		global.mosaicPaneProperty().addListener((obs, ov, nv) -> {
			if (nv != null) {
				root.setBottom(bottom);
			} else {
				root.setBottom(null);
			}
		});

		if (!getParameters().getUnnamed().isEmpty()) {
			Loader.load(new File(getParameters().getUnnamed().get(0)));
		}

		Preferences prefs = Preferences.userNodeForPackage(FXApplication.class);
		double width = prefs.getDouble(WIDTH_KEY, WIDTH_DEF_VALUE);
		double height = prefs.getDouble(HEIGHT_KEY, HEIGHT_DEF_VALUE);
		boolean maximized = prefs.getBoolean(MAXIMIZED_KEY, false);

		Scene s = new Scene(root, width, height);
		hookupDragHandlers(s);

		s.setOnKeyPressed(evt -> {
			if (evt.getCode() == KeyCode.F11)
				primaryStage.setFullScreen(!primaryStage.isFullScreen());
		});

		primaryStage.setMaximized(maximized);
		primaryStage.setFullScreenExitHint("Press F11 to exit fullscreen mode");
		primaryStage.setTitle("Mosaic");
		primaryStage.setMinWidth(620);
		primaryStage.setMinHeight(660);
		primaryStage.setScene(s);
		primaryStage.show();

		primaryStage.setOnCloseRequest(evt -> {
			prefs.putBoolean(MAXIMIZED_KEY, primaryStage.isMaximized());

			if (!primaryStage.isMaximized()) {
				prefs.putDouble(WIDTH_KEY, s.getWidth());
				prefs.putDouble(HEIGHT_KEY, s.getHeight());
			}
		});
	}

	private boolean draggingOut;

	private void hookupDragHandlers(Scene s) {

		s.setOnDragDetected(evt -> {
			if (global.getMosaicPane() == null) {
				return;
			}

			Dragboard db = s.startDragAndDrop(TransferMode.COPY);
			ClipboardContent c = new ClipboardContent();
			c.putString(global.getMosaicPane().getMosaic().toCSV());
			db.setContent(c);

			Text txt = new Text(" \u21f1  Export ");
			txt.setFill(Color.BLUE);

			SnapshotParameters params = new SnapshotParameters();
			params.setFill(Color.WHITE);
			WritableImage img = txt.snapshot(params, null);

			PixelWriter writer = img.getPixelWriter();
			for (int i = 0; i < img.getWidth(); i++) {
				writer.setColor(i, 0, Color.BLUE);
				writer.setColor(i, (int) img.getHeight() - 1, Color.BLUE);
			}
			for (int i = 0; i < img.getHeight(); i++) {
				writer.setColor(0, i, Color.BLUE);
				writer.setColor((int) img.getWidth() - 1, i, Color.BLUE);
			}

			db.setDragView(img, -5, -5);

			draggingOut = true;
		});

		s.setOnDragDone(evt -> {
			draggingOut = false;
		});

		s.setOnDragOver(evt -> {
			if (!draggingOut && isValidClipboardContent(evt.getDragboard())) {
				evt.acceptTransferModes(TransferMode.ANY);
			}

		});

		s.setOnDragDropped(evt -> {
			evt.setDropCompleted(importClipboardContent(evt.getDragboard(), true));
		});

	}

	private boolean isValidClipboardContent(Clipboard clipboard) {
		if (clipboard.hasFiles()) {
			return clipboard.getFiles().size() == 1 && Loader.isSupportedFileExtension(clipboard.getFiles().get(0));
		} else {
			return clipboard.hasImage() || clipboard.hasString();
		}
	}

	private boolean importClipboardContent(Clipboard clipboard, boolean warning) {
		if (warning && !Loader.overwrite())
			return false;

		if (clipboard.hasFiles()) {
			File f = clipboard.getFiles().get(0);
			Loader.load(f);
		} else if (clipboard.hasImage()) {
			Loader.loadImage(clipboard.getImage());
		} else if (clipboard.hasString()) {
			Loader.loadString(clipboard.getString());
		} else {
			return false;
		}

		return true;
	}

	public static void showDocument(String url) {
		if (app != null)
			app.getHostServices().showDocument(url);
	}

	public static void main(String[] args) {
		Thread singleInstance = new Thread(() -> {
			try (ServerSocket ss = new ServerSocket(9090, 0, InetAddress.getByName(null))) {
				while (true) {
					Socket socket = ss.accept();
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

					String remoteArg = in.readLine();
					Platform.runLater(() -> {
						if (Loader.overwrite())
							Loader.load(new File(remoteArg));
					});

					in.close();
					socket.close();
				}
			} catch (IOException e) {
				if (args.length > 0) {
					// pass over to running instance
					try (Socket socket = new Socket("localhost", 9090);
							PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()))) {
						out.println(args[0]);
					} catch (IOException ioe) {
						e.printStackTrace();
					}
				}
				System.exit(0);
			}
		});
		singleInstance.setDaemon(true);
		singleInstance.start();

		launch(args);
	}
}
