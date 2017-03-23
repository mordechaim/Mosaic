package com.stackexchange.puzzling.user.mordechai.mosaic.solvers;

import com.stackexchange.puzzling.user.mordechai.mosaic.Mosaic;

public class IllegalClueStateException extends IllegalStateException {

	private static final long serialVersionUID = -2631741709643143512L;
	
	private final int x;
	private final int y;
	private final Mosaic mosaic;
	
	public IllegalClueStateException(Mosaic mosaic, int x, int y) {
		super("x: " + x + ", y: " + y);
		this.mosaic = mosaic;
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public Mosaic getMosaic() {
		return mosaic;
	}
}
