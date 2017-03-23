package com.stackexchange.puzzling.user.mordechai.mosaic.solvers;

import com.stackexchange.puzzling.user.mordechai.mosaic.Mosaic;

public class ContradictionException extends IllegalClueStateException {

	private static final long serialVersionUID = 6009224406992725520L;

	public ContradictionException(Mosaic mosaic, int x, int y) {
		super(mosaic,x, y);
	}

}
