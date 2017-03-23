package com.stackexchange.puzzling.user.mordechai.mosaic;

import java.io.IOException;
import java.util.function.Supplier;

import com.stackexchange.puzzling.user.mordechai.grid.Grid;
import com.stackexchange.puzzling.user.mordechai.mosaic.solvers.RecursionSolver;

public class TestPuzzle {

	public static void main(String[] args) throws IOException {
		
		Mosaic p = new Mosaic(TestPuzzle.class.getResource("face.png"));
		p.putClues((grid, x, y, iteration) -> (x+y) % 5 == iteration, 2);


		System.out.println(p.getGrid().toGridString(clue -> " " + (clue.getClue() == -1 ? " " : clue.getClue()) , false));
		
		RecursionSolver a = new RecursionSolver(p);
		a.checkAmbiguity(true);
		a.start();

		System.out.println(p.getGrid().toGridString(clue -> {
			if (clue.isPixel())
				return " " + "*";
			return "  ";
			// if (clue.getClue() == -1)
			// return " ";
			// return " " + clue.getClue();
		}, false));
		
		System.out.println(a.getReport());

		System.out.println(p.getGrid().toGridString(clue -> " " + clue.getFill(), false));
	}
}
