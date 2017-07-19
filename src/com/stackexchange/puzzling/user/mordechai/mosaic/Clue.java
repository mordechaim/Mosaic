package com.stackexchange.puzzling.user.mordechai.mosaic;

public class Clue {

	private Fill fill = Fill.EMPTY;
	private int clue = -1;
	private boolean isPixel;

	public Clue(Fill fill, int clue, boolean isPixel) {
		setFill(fill);
		setClue(clue);
		setIsPixel(isPixel);
	}

	public Clue(boolean isPixel) {
		setIsPixel(isPixel);
	}

	public Clue(Clue other) {
		if(other == null)
			return;

		setFill(other.fill);
		setClue(other.clue);
		setIsPixel(other.isPixel);
	}

	public Clue() {
	}

	public Clue copy() {
		return new Clue(this);
	}

	public void merge(Clue other) {
		setClue(other.getClue());
		setFill(other.getFill());
		setIsPixel(other.isPixel());
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

	@Override
	public String toString() {
		return toString(true, true, true);
	}

	public String toString(boolean fill, boolean clue, boolean pixel) {

		StringBuilder builder = new StringBuilder();
		if (clue && getClue() >= 0)
			builder.append(getClue());
		if (fill)
			builder.append(getFill());
		if (pixel && isPixel())
			builder.append("*");

		return builder.toString();
	}

	public static Clue fromString(String str) {
		Clue clue = new Clue();

		clue.setIsPixel(str.contains("*"));
		if (str.contains("#"))
			clue.setFill(Fill.FILLED);
		else if (str.contains("X"))
			clue.setFill(Fill.X);

		if (str.length() > 0) {
			char ch = str.charAt(0);
			if (Character.isDigit(ch)) {
				clue.setClue(ch - '0');
			}
		}

		return clue;
	}
}
