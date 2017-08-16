package com.stackexchange.puzzling.user.mordechai.mosaic.fx;

import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

public class Description extends BorderPane {

	private static final String NEWLINE = "\n\n\t";
	private static final String BULLET = "\n\t\u2022 ";

	public Description() {
		ImageView logo = new ImageView(getClass().getResource("resources/Mosaic.png").toExternalForm());
		ImageView github = new ImageView(getClass().getResource("resources/GitHub-Mark-32px.png").toExternalForm());
		logo.setFitWidth(75);
		logo.setFitHeight(75);
		github.setFitWidth(25);
		github.setFitHeight(25);
		Hyperlink ghLink = link("Github", "https://github.com/mordechaim/Mosaic");
		ghLink.setGraphic(github);

		TextFlow flow = new TextFlow();
		Separator s = new Separator();
		s.prefWidthProperty().bind(flow.widthProperty());

		flow.getChildren().addAll(boldText("Mosaic"),
				text("   —   Also known as: ArtMosaico, Count and Darken, Cuenta Y Sombrea, Fill-a-Pix, Fill-In,"
						+ " Komsu Karala, Magipic, Majipiku, Mosaico, Mosaik, Mozaiek, Nampre Puzzle, Nurie-Puzzle, Oekaki-Pix,"
						+ " Voisimage." + NEWLINE
						+ "Mosaic is a Minesweeper-like puzzle based on a grid with a pixel-art picture hidden"
						+ " inside. Using logic alone, the solver determines which squares are painted and which should remain"
						+ " empty until the hidden picture is completely exposed." + NEWLINE
						+ "Each puzzle consists of a grid containing clues in various places. The object is to reveal "
						+ "a hidden picture by painting the squares around each clue so that the number of painted"
						+ " squares, including the square with the clue, matches the value of the clue." + NEWLINE
						+ "Originally created by "),
				link("Trevor Truran", "https://en.wikipedia.org/wiki/Trevor_Truran"), text(", after inspiration of "),
				link("Conway's Game of Life", "https://en.wikipedia.org/wiki/Conway's_Game_of_Life"),
				text("." + NEWLINE + "The puzzle was later developed by "),
				link("ConceptisPuzzles", "http://www.conceptispuzzles.com/index.aspx?uri=puzzle/fill-a-pix"),
				text(" under the name "), boldText("Fill-a-Pix"), text(", following the \"a-pix\" pixel art series:\n"),
				boldBullet(), boldLink("Pic-A-Pix", "http://www.conceptispuzzles.com/index.aspx?uri=puzzle/pic-a-pix"),
				text("   —   Nonogram"), boldBullet(),
				boldLink("Sym-a-Pix", "http://www.conceptispuzzles.com/index.aspx?uri=puzzle/sym-a-pix"), boldBullet(),
				boldLink("Link-a-Pix", "http://www.conceptispuzzles.com/index.aspx?uri=puzzle/link-a-pix"),
				boldBullet(),
				boldLink("Maze-a-Pix", "http://www.conceptispuzzles.com/index.aspx?uri=puzzle/maze-a-pix"),
				boldBullet(), boldLink("Dot-a-Pix", "http://www.conceptispuzzles.com/index.aspx?uri=puzzle/dot-a-pix"),
				boldBullet(),
				boldLink("Cross-a-Pix", "http://www.conceptispuzzles.com/index.aspx?uri=puzzle/cross-a-pix"),
				boldBullet(),
				boldLink("Block-a-Pix", "http://www.conceptispuzzles.com/index.aspx?uri=puzzle/block-a-pix"),
				text(NEWLINE
						+ "Here are some links to understand the rules of the puzzle — all from ConceptisPuzzles:\n"),
				bullet(),
				link("Mosaic Rules", "http://www.conceptispuzzles.com/index.aspx?uri=puzzle/fill-a-pix/rules"),
				bullet(),
				link("Animated Solving", "http://www.conceptispuzzles.com/index.aspx?uri=puzzle/fill-a-pix/tutorial"),
				text("  —   Requires Flash"), bullet(),
				link("Basic Logic and Advanced Logic Techniques",
						"http://www.conceptispuzzles.com/index.aspx?uri=puzzle/fill-a-pix/techniques"),
				bullet(), link("Puzzle Tips", "http://www.conceptispuzzles.com/index.aspx?uri=puzzle/fill-a-pix/tips"),
				bullet(),
				link("Puzzle History", "http://www.conceptispuzzles.com/index.aspx?uri=puzzle/fill-a-pix/history"),
				text("\n\n"), s, text(NEWLINE + "Read more about this project on "), ghLink, text("."));

		flow.setTextAlignment(TextAlignment.JUSTIFY);
		flow.setMaxWidth(550);

		StackPane sp = new StackPane(logo);
		sp.setPadding(new Insets(15));

		setTop(sp);
		setCenter(flow);
		getStyleClass().add("description");
		getStylesheets().add(getClass().getResource("resources/css/description-defaults.css").toExternalForm());
	}

	private static Text text(String text) {
		Text txt = new Text(text);
		txt.getStyleClass().add("text");

		return txt;
	}

	private static Text boldText(String text) {
		Text txt = text(text);
		txt.getStyleClass().add("bold");

		return txt;
	}

	private static Hyperlink link(String text, String url) {
		Hyperlink link = new Hyperlink(text);
		link.setOnAction(evt -> FXApplication.showDocument(url));
		link.setTooltip(new Tooltip(url));

		return link;
	}

	private static Hyperlink boldLink(String text, String url) {
		Hyperlink link = link(text, url);
		link.getStyleClass().add("bold");

		return link;
	}

	private static Text bullet() {
		return text(BULLET);
	}

	private static Text boldBullet() {
		return boldText(BULLET);
	}
}
