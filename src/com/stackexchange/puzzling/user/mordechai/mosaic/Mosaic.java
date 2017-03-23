package com.stackexchange.puzzling.user.mordechai.mosaic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.stackexchange.puzzling.user.mordechai.grid.Grid;

public class Mosaic {

	private Grid<Clue> grid;

	public Mosaic(Image image) {
		grid = new Grid<>(image.getWidth(null), image.getHeight(null));

		BufferedImage blackAndWhite = new BufferedImage(image.getWidth(null), image.getHeight(null),
				BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D g2d = blackAndWhite.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, image.getWidth(null), image.getHeight(null));
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();

		grid.fill((x, y, old) -> new Clue(blackAndWhite.getRGB(x, y) != 0xffffffff));
	}

	public Mosaic(URL url) throws IOException {
		this(ImageIO.read(url));
	}

	public Mosaic(URL url, int width, int height) throws IOException {
		this(ImageIO.read(url).getScaledInstance(width, height, Image.SCALE_SMOOTH));
	}
	
	public Mosaic(Mosaic other) {
		grid = other.getGrid().branch(Clue::new);
	}
	
	public Mosaic(Grid<Clue> grid) {
		this.grid = grid;
	}

	public Grid<Clue> getGrid() {
		return grid;
	}

	public Grid<Clue> getSurroundingCells(int x, int y) {
		int width = 3;
		int height = 3;

		if (x > 0)
			x--;
		else
			width--;

		if (y > 0)
			y--;
		else
			height--;

		while (x + width > grid.getWidth())
			width--;
		while (y + height > grid.getHeight())
			height--; // FIXME

		return grid.view(x, y, width, height);
	}

	public void putClues(ClueGenerator generator, int iterations) {
		if (generator.iterations() > 0)
			iterations = generator.iterations();

		if (iterations <= 0)
			throw new IllegalArgumentException("iterations: " + iterations + " < 1");

		for (int i = 1; i <= iterations; i++) {
			for (int y = 0; y < grid.getHeight(); y++) {
				for (int x = 0; x < grid.getWidth(); x++) {
					if (generator.shouldGenerate(grid, x, y, i))
						putClue(x, y);
				}
			}
		}
	}

	public void putClues(ClueGenerator generator) {
		putClues(generator, 1);
	}

	private void putClue(int x, int y) {
		Grid<Clue> sub = getSurroundingCells(x, y);

		Clue clue = grid.get(x, y);
		int count = sub.count(c -> c.isPixel());
		clue.setClue(count);

	}

}
