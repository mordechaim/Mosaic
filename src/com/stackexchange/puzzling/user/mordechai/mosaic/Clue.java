package com.stackexchange.puzzling.user.mordechai.mosaic;

public class Clue {

	private Fill fill = Fill.EMPTY;
	private int clue = -1;
	private boolean isPixel;

	public Clue(Fill fill, int clue, boolean isPixel) {
		this.fill = fill;
		this.clue = clue;
		this.isPixel = isPixel;
	}

	public Clue(boolean isPixel) {
		this.isPixel = isPixel;
	}

	public Clue(Clue other) {
		if (other == null)
			return;

		this.fill = other.fill;
		this.clue = other.clue;
		this.isPixel = other.isPixel;
	}

	public Clue() {
	}

	public Fill getFill() {
		return fill;
	}

	public int getClue() {
		return clue;
	}

	public boolean isPixel() {
		return isPixel;
	}

	public void setFill(Fill fill) {
		this.fill = fill;
	}

	public void setClue(int clue) {
		this.clue = clue;
	}

	public void setIsPixel(boolean b) {
		isPixel = b;
	}

}
