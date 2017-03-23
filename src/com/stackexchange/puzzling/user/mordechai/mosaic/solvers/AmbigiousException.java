package com.stackexchange.puzzling.user.mordechai.mosaic.solvers;

import com.stackexchange.puzzling.user.mordechai.mosaic.Mosaic;

public class AmbigiousException extends IllegalClueStateException {

	private static final long serialVersionUID = 4810438065721867118L;
	
	private final Mosaic other;

	public AmbigiousException(Mosaic mosaic1, Mosaic mosaic2, int x, int y) {
		super(mosaic1, x, y);
		other = mosaic2;
	}
	
	public Mosaic getOtherMosaic() {
		return other;
	}

}
