package com.stackexchange.puzzling.user.mordechai.mosaic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

import javax.imageio.ImageIO;

import com.stackexchange.puzzling.user.mordechai.grid.Grid;
import com.stackexchange.puzzling.user.mordechai.grid.GridIterator;
import com.stackexchange.puzzling.user.mordechai.mosaic.solvers.IllegalClueStateException;
import com.stackexchange.puzzling.user.mordechai.mosaic.solvers.RecursionSolver;

public class Mosaic implements Iterable<Clue> {

	private Grid<Clue> grid;

	public static Mosaic loadScaledImage(Image image, int width, int height) {
		Grid<Clue> grid = new Grid<>(width, height);

		BufferedImage blackAndWhite = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D g2d = blackAndWhite.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g2d.fillRect(0, 0, width, height);
		g2d.drawImage(image, 0, 0, width, height, null);
		g2d.dispose();

		grid.fill((x, y, old) -> new Clue(blackAndWhite.getRGB(x, y) != 0xffffffff));

		return new Mosaic(grid);
	}

	public static Mosaic loadImage(Image image) {
		return loadScaledImage(image, image.getWidth(null), image.getHeight(null));
	}

	public static Mosaic loadImage(InputStream in) throws IOException {
		return loadImage(ImageIO.read(in));
	}

	public static Mosaic loadScaledImage(InputStream in, int width, int height) throws IOException {
		return loadScaledImage(ImageIO.read(in), width, height);
	}

	public static Mosaic fromString(String str) {
		if (str.contains(",") || str.contains("\t"))
			return fromCSV(str);

		return fromClues(str);
	}

	public static Mosaic loadString(InputStream in) throws IOException {
		Scanner scn = new Scanner(in);
		String content = scn.useDelimiter("\\Z").next();
		scn.close();

		return fromString(content);
	}

	public static Mosaic loadString(File file) throws IOException {
		Scanner scn = new Scanner(file);
		String content = scn.useDelimiter("\\Z").next();
		scn.close();

		return fromString(content);
	}

	public static Mosaic fromClues(String str) {
		Mosaic mosaic = new Mosaic(Grid.fromString(str, "", (x, y, token) -> Clue.fromString(token)));
		mosaic.grid().fill((x, y, old) -> old == null ? new Clue() : old);

		return mosaic;
	}

	public static Mosaic loadClues(InputStream in) throws IOException {
		Scanner scn = new Scanner(in);
		String content = scn.useDelimiter("\\Z").next();
		scn.close();

		return fromClues(content);
	}

	public static Mosaic loadClues(File file) throws IOException {
		Scanner scn = new Scanner(file);
		String content = scn.useDelimiter("\\Z").next();
		scn.close();

		return fromClues(content);
	}

	public String toClues() {
		return grid.toGridString(clue -> clue.getClue() >= 0 ? String.valueOf(clue.getClue()) : " ", "");
	}

	public static Mosaic fromCSV(String str) {
		Mosaic mosaic = new Mosaic(Grid.fromString(str, (x, y, token) -> Clue.fromString(token)));
		mosaic.grid().fill((x, y, old) -> old == null ? new Clue() : old);

		return mosaic;
	}

	public static Mosaic loadCSV(InputStream in) throws IOException {
		Scanner scn = new Scanner(in);
		String content = scn.useDelimiter("\\Z").next();
		scn.close();

		return fromCSV(content);
	}

	public static Mosaic loadCSV(File file) throws IOException {
		Scanner scn = new Scanner(file);
		String content = scn.useDelimiter("\\Z").next();
		scn.close();

		return fromCSV(content);
	}

	public String toCSV() {
		return grid.toString();
	}

	public String toCSV(boolean fill, boolean clue, boolean pixel) {
		return grid.toGridString(c -> c == null ? "" : c.toString(fill, clue, pixel), ",");
	}

	@Override
	public String toString() {
		return toCSV();
	}

	public Mosaic(Mosaic other) {
		grid = other.grid().branch(Clue::copy);
	}

	public Mosaic(int width, int height) {
		grid = new Grid<>(width, height);
		grid.fill(() -> new Clue());
	}

	public Mosaic(Grid<Clue> grid) {
		this.grid = grid;
	}

	public Grid<Clue> grid() {
		return grid;
	}

	/*
	 * Convenient delegate methods.
	 */

	public int width() {
		return grid.width();
	}

	public int height() {
		return grid.height();
	}

	public int length() {
		return grid.length();
	}

	public Clue get(int linear) {
		return grid.get(linear);
	}

	public Clue get(int x, int y) {
		return grid.get(x, y);
	}

	@Override
	public GridIterator<Clue> iterator() {
		return grid.iterator();
	}

	public Grid<Clue> getSurroundingCells(int x, int y) {
		return grid.getSurroundingCells(x, y);
	}

	public int count(Predicate<Clue> tester) {
		return grid.count(tester);
	}

	public void fillClues() {
		grid.forEach((cell, x, y) -> putClue(x, y));
	}

	public void putClue(int x, int y) {
		Grid<Clue> sub = grid.getSurroundingCells(x, y);

		Clue clue = grid.get(x, y);
		int count = sub.count(c -> c.isPixel());
		clue.setClue(count);

	}

	public void removeClue(int x, int y) {
		grid().get(x, y).setClue(-1);
	}

	private void putMinimumClues(float level, boolean refillClues) {
		clueGenerator(level, refillClues).forEachRemaining(n -> {
		});
	}

	public Iterator<Integer> clueGenerator(float level, boolean refillClues) {
		return new Iterator<Integer>() {

			float localLevel;
			int cluesWithBasic;
			List<Integer> cells;
			int i;

			{
				if (refillClues) {
					fillClues();
				}

				// negate level (0.6 to 0.4)
				localLevel = Math.abs(level - 1);

				cells = new ArrayList<>(grid.length());
				for (int i = 0; i < grid.length(); i++) {
					if (grid.get(i).getClue() >= 0)
						cells.add(i);
				}

				Collections.shuffle(cells);

				float size = cells.size();
				cluesWithBasic = (int) (size * localLevel);
			}

			@Override
			public boolean hasNext() {
				return i < cells.size();
			}

			@Override
			public Integer next() {
				IllegalClueStateException e = tryRemove(cells.get(i), i > cluesWithBasic);
				String type = i <= cluesWithBasic ? "BASIC" : "ADVANCED";
				if (e == null)
					System.out.print(type + " " + new Date() + ": " + (i + 1) + "/" + cells.size() + " COMMIT");
				else {
					System.out.println(e);
					System.out.print(type + " " + new Date() + ": " + (i + 1) + "/" + cells.size() + " ROLLBACK");

				}
				System.out.println(" - x,y: " + cells.get(i) % grid.width() + "," + cells.get(i) / grid.width());
				i++;

				return cells.get(i - 1);
			}

		};
	}

	public Iterator<Integer> clueGenerator(float level) {
		return clueGenerator(level, true);
	}

	private IllegalClueStateException tryRemove(int i, boolean advancedLogic) {
		Clue clue = grid.get(i);
		int oldVal = clue.getClue();
		clue.setClue(-1);

		try {
			RecursionSolver solver = new RecursionSolver(this);
			solver.useAdvancedLogic(advancedLogic);
			solver.checkAmbiguity(true);
			solver.start();
			return null;
		} catch (IllegalClueStateException e) {
			clue.setClue(oldVal);
			return e;
		} finally {
			grid.forEach(cl -> cl.setFill(Fill.EMPTY));
		}
	}

	public void putBasicClues(boolean refillClues) {
		putMinimumClues(0f, refillClues);
	}

	public void putBasicClues() {
		putBasicClues(true);
	}

	public void putAdvancedClues(float level, boolean refillClues) {
		putMinimumClues(level, refillClues);
	}

	public void putAdvancedClues(float level) {
		putAdvancedClues(level, true);
	}
}
