package com.stackexchange.puzzling.user.mordechai.grid;

public class Matrix<T> {

	private int width;
	private int height;

	private Cell<T>[][] cells;

	@SuppressWarnings("unchecked")
	public Matrix(int width, int height) {
		this.width = width;
		this.height = height;

		cells = new Cell[width][height];
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cells[i][j] = new Cell<T>(i, j);
			}
		}
	}

	public Cell<T> cellAt(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height)
			return null;

		return cells[x][y];
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean hasCellAt(int x, int y) {
		return cellAt(x, y) != null;
	}

}
