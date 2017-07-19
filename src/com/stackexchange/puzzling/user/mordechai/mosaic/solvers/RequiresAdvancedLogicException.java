package com.stackexchange.puzzling.user.mordechai.mosaic.solvers;

import com.stackexchange.puzzling.user.mordechai.mosaic.Mosaic;

public class RequiresAdvancedLogicException extends IllegalClueStateException {

	public RequiresAdvancedLogicException(Mosaic mosaic, int x, int y) {
		super(mosaic, x, y);
	}
}
