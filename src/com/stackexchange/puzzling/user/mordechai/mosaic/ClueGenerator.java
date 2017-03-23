package com.stackexchange.puzzling.user.mordechai.mosaic;

import com.stackexchange.puzzling.user.mordechai.grid.Grid;

public interface ClueGenerator {

	boolean shouldGenerate(Grid<Clue> grid, int x, int y, int iteration);
	
	default int iterations() {
		return 0;
	}
}
