package com.stackexchange.puzzling.user.mordechai.mosaic.solvers;

import com.stackexchange.puzzling.user.mordechai.mosaic.Mosaic;

public class NoClueException extends IllegalClueStateException {

	private static final long serialVersionUID = 4204690721535781349L;

	public NoClueException(Mosaic mosaic, int x, int y) {
		super(mosaic, x, y);
	}

}
