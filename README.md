**Mosaic** &nbsp; &mdash; &nbsp; 
Also known as:
ArtMosaico, Count and Darken, Cuenta Y Sombrea, Fill-a-Pix, Fill-In, Komsu Karala, Magipic, Majipiku, Mosaico, Mosaik, Mozaiek, Nampre Puzzle, Nurie-Puzzle, Oekaki-Pix, Voisimage.

Mosaic is a Minesweeper-like puzzle based on a grid with a pixel-art picture hidden inside. Using logic alone, the solver determines which squares are painted and which should remain empty until the hidden picture is completely exposed.

Each puzzle consists of a grid containing clues in various places. The object is to reveal a hidden picture by painting the squares around each clue so that the number of painted squares, including the square with the clue, matches the value of the clue.

Originally created by [Trevor Truran](https://en.wikipedia.org/wiki/Trevor_Truran), after inspiration of [Conway's Game of Life](https://en.wikipedia.org/wiki/Conway's_Game_of_Life). 

The puzzle was later developed by [ConceptisPuzzles](http://www.conceptispuzzles.com/index.aspx?uri=puzzle/fill-a-pix) under the name __Fill-a-Pix__, following the "a-pix" pixel art series:

- [**Pic-a-Pix**](http://www.conceptispuzzles.com/index.aspx?uri=puzzle/pic-a-pix) &nbsp; &mdash; &nbsp; Nonogram
- [**Sym-a-Pix**](http://www.conceptispuzzles.com/index.aspx?uri=puzzle/sym-a-pix)
- [**Link-a-Pix**](http://www.conceptispuzzles.com/index.aspx?uri=puzzle/link-a-pix)

- [**Maze-a-Pix**](http://www.conceptispuzzles.com/index.aspx?uri=puzzle/maze-a-pix)
- [**Dot-a-Pix**](http://www.conceptispuzzles.com/index.aspx?uri=puzzle/dot-a-pix)
- [**Cross-a-Pix**](http://www.conceptispuzzles.com/index.aspx?uri=puzzle/cross-a-pix)
- [**Block-a-Pix**](http://www.conceptispuzzles.com/index.aspx?uri=puzzle/block-a-pix)

I'll stick to the name "Mosaic" as the general name of this puzzle.

Here are some links to understand the rules of the puzzle &mdash; all from ConceptisPuzzles:

- [Mosaic Rules](http://www.conceptispuzzles.com/index.aspx?uri=puzzle/fill-a-pix/rules)
- [Animated Solving](http://www.conceptispuzzles.com/index.aspx?uri=puzzle/fill-a-pix/tutorial) &nbsp; &mdash; &nbsp; Requires Flash
- [Basic Logic and Advanced Logic Techniques](http://www.conceptispuzzles.com/index.aspx?uri=puzzle/fill-a-pix/techniques)
- [Puzzle Tips](http://www.conceptispuzzles.com/index.aspx?uri=puzzle/fill-a-pix/tips)
- [Puzzle History](http://www.conceptispuzzles.com/index.aspx?uri=puzzle/fill-a-pix/history)

----------

Creating your own Mosaic
-

There are 3 rules that must be met when creating a Mosaic. Trying to solve them using the API (below) will result in its appropriate exception to be thrown.

- **Every cell must have at-least one clue, either surrounding or in itself.**<br>
[![no-clue][1]][1]
 
 As you can see, the 3 right cells will never be solvable.
 Trying to solve this with the API will throw:

        Exception in thread "main" com.stackexchange.puzzling.user.mordechai.mosaic.solvers.NoClueException: x: 3, y: 0
            at com.stackexchange.puzzling.user.mordechai.mosaic.solvers.AbstractSolveAlgorithm.<init>(AbstractSolveAlgorithm.java:37)
            at com.stackexchange.puzzling.user.mordechai.mosaic.solvers.RecursionSolver.<init>(RecursionSolver.java:32)
            at com.stackexchange.puzzling.user.mordechai.mosaic.solvers.RecursionSolver.<init>(RecursionSolver.java:28)
            at com.stackexchange.puzzling.user.mordechai.mosaic.TestPuzzle.main(TestPuzzle.java:14)



- **Clues must not contradict.**<br>
[![contradiction][2]][2]

 Since 9 means all cells painted, the 2 will have 3 painted cells, which contradicts with the clue.
 Trying to solve this with the API will throw:

        Exception in thread "main" com.stackexchange.puzzling.user.mordechai.mosaic.solvers.ContradictionException: x: 3, y: 1
	        at com.stackexchange.puzzling.user.mordechai.mosaic.solvers.RecursionSolver.takeStepImpl(RecursionSolver.java:151)
	        at com.stackexchange.puzzling.user.mordechai.mosaic.solvers.RecursionSolver.takeStep(RecursionSolver.java:127)
	        at com.stackexchange.puzzling.user.mordechai.mosaic.solvers.RecursionSolver.run(RecursionSolver.java:90)
	        at com.stackexchange.puzzling.user.mordechai.mosaic.solvers.RecursionSolver.start(RecursionSolver.java:56)
	        at com.stackexchange.puzzling.user.mordechai.mosaic.TestPuzzle.main(TestPuzzle.java:23)


- **Must have a unique solution.** <br> If not enough clues are given there may be cases where 2 or more possible solutions are all correct according to the clues.
[![unique][3]][3]

 Once again, trying to solve a puzzle like this with this with `checkAmbiguity(true)`, will throw:

        Exception in thread "main" com.stackexchange.puzzling.user.mordechai.mosaic.solvers.AmbigiousException: x: 3, y: 1
	        at com.stackexchange.puzzling.user.mordechai.mosaic.solvers.RecursionSolver.lambda$5(RecursionSolver.java:224)
	        at com.stackexchange.puzzling.user.mordechai.mosaic.solvers.AbstractSolveAlgorithm.fire(AbstractSolveAlgorithm.java:175)
	        at com.stackexchange.puzzling.user.mordechai.mosaic.solvers.AbstractSolveAlgorithm.setState(AbstractSolveAlgorithm.java:82)
	        at com.stackexchange.puzzling.user.mordechai.mosaic.solvers.RecursionSolver.setState(RecursionSolver.java:283)
	        at com.stackexchange.puzzling.user.mordechai.mosaic.solvers.RecursionSolver.takeStep(RecursionSolver.java:119)
	        at com.stackexchange.puzzling.user.mordechai.mosaic.solvers.RecursionSolver.takeStep(RecursionSolver.java:104)
	        at com.stackexchange.puzzling.user.mordechai.mosaic.solvers.RecursionSolver.takeStep(RecursionSolver.java:104)
	        at com.stackexchange.puzzling.user.mordechai.mosaic.solvers.RecursionSolver.run(RecursionSolver.java:90)
	        at com.stackexchange.puzzling.user.mordechai.mosaic.solvers.RecursionSolver.start(RecursionSolver.java:56)
	        at com.stackexchange.puzzling.user.mordechai.mosaic.TestPuzzle.main(TestPuzzle.java:23)


Using the Application Programming Interface (API)
-

<h3>1. Kickstarter </h3>

The entry point is the class `Mosaic` that takes a `java.awt.Image`, URL or a `Grid<Clue>` - the logical representation of a grid used internally in `Mosaic` - as its constructor's parameters. The `Grid` will be filled with `Clues`. Here's the `Clue` class:


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


`Fill` is an enum with values `FILLED, EMPTY, X;`

**1.1 Filling Clues**

The `Mosaic` class itself just sets the `isPixel` of each clue according to the image, but the actual clues is up to the programmer to do. `Mosaic` has a method:

    public void putClues(ClueGenerator generator)

Which helps fill them up neatly.

`ClueGenerator` is a functional interface:

    package com.stackexchange.puzzling.user.mordechai.mosaic;

    import com.stackexchange.puzzling.user.mordechai.grid.Grid;

    public interface ClueGenerator {
    
        boolean shouldGenerate(Grid<Clue> grid, int x, int y, int iteration);
    	
    	default int iterations() {
            return 0;
    	}
    }

Here's an example of creating and filling the clues with a checkerboard design:

<pre>Mosaic m = new Mosaic(getClass().getResource("empty-8x8.png"));
// empty-50x50.png is just a white empty image of size 8,8

<b>m.putClues((grid, x, y, iteration) -> (x+y) % 2 == 0);</b>

System.out.println(m.getGrid().toGridString(
                   clue -> " " + (clue.getClue() == -1 ? " " : clue.getClue()),
                   false)); // true would return with grid-lines</pre>

The above code prints to the console:

     0   0   0   0  
       0   0   0   0
     0   0   0   0  
       0   0   0   0
     0   0   0   0  
       0   0   0   0
     0   0   0   0  
       0   0   0   0


You can as well do random fills or even make the clues be an image itself:

   
<pre>Grid<Clue> grid = new Grid<>(20, 20);
grid.fill(Clue::new);

Mosaic m = new Mosaic(grid);

BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
Graphics g = image.getGraphics();

g.setColor(Color.WHITE);
g.fillRect(0, 0, 20, 20);
g.setColor(Color.BLACK);
g.setFont(new Font("Dialog", Font.BOLD, 25));
g.drawString("?", 2, 19);

g.dispose();

Mosaic q = new Mosaic(image);
m.putClues((gr, x, y, iteration) -> <b>q.getGrid().get(x, y).isPixel()</b> || (x+y) % 4 == 0);

System.out.println(m.getGrid().toGridString(
                   clue -> " " + (clue.getClue() == -1 ? " " : clue.getClue()),
                   false));</pre>

Results:

     0       0       0       0       0      
           0       0 0 0 0 0       0       0
         0     0 0 0 0 0 0 0 0 0 0       0  
       0     0 0 0 0 0 0 0 0 0 0 0     0    
     0     0 0 0 0   0       0 0 0 0 0      
           0 0 0   0       0   0 0 0       0
         0       0       0     0 0 0     0  
       0       0       0     0 0 0 0   0    
     0       0       0     0 0 0 0   0      
           0       0     0 0 0 0   0       0
         0       0     0 0 0 0   0       0  
       0       0     0 0 0 0   0       0    
     0       0       0 0 0   0       0      
           0       0 0 0 0 0       0       0
         0       0       0       0       0  
       0       0       0       0       0    
     0       0       0 0 0   0       0      
           0       0 0 0 0 0       0       0
         0       0   0 0 0       0       0  
       0       0       0       0       0    

Ideas are virtually endless, show your creativity here.

**1.2 Iterations**

In some cases you will want to repeatedly iterate over the grid when filling the clues until some precondition is met (e.g. Fill randomly, then repeat and check if a cell has a missing clue).

There's an overloaded version of `putClues()`:
  
    public void putClues(ClueGenerator generator, int iterations)

The process will repeat $n$ times according to `iterations`. You can get the current iteration in the last argument of the clue generator.

In the event that you want to write a general, full concrete implementation of `ClueGenerator` that heavily relies in the amount of iterations, you can override the default `iterations()` method. Any number greater that zero will always have precedence over the iterations parameter in `putClues()`. Zero or below will either use the `iterations` param or default to 1 if the first overload is used.

<h3>2. Solving</h3>

To the current time, I've implemented one solve algorithm, namely `RecursionSolver`. As its name tells, it uses recursion. The regular case doesn't really require recursion to solve; it's the [Advanced Logic](http://www.conceptispuzzles.com/index.aspx?uri=puzzle/fill-a-pix/techniques#advancedlogic) problems that recursion will be used for.

Using it is easy, just create an instance, pass the `Mosaic` as constructor argument and start.

    Mosaic m = new Mosaic(...);
    RecursionSolver rs = new RecursionSolver(m);
    rs.start();

You can then output the results to the console:


    System.out.println(m.getGrid().toGridString(clue -> " " + clue.getFill(), false));

And a report:

    System.out.println(rs.getReport());

**2.1 Uniqueness**

As explained earlier, puzzles must follow some rules to be valid. Contradictions and missing clues are detected easily, but uniqueness requires to try all valid solutions and check if more than one is found.

For this reason, I've designed the solver that uniqueness detection is off by default. You can toggle that behaviour:

    rs.checkAmbiguity(aBoolean);

<h3>3. Exporting to Excel</h3>

After you've created a Mosaic but you want to put it into Excel, you have to print a CSV version and just copy-paste it:

<pre>System.out.println(m.getGrid().toGridString(
                   clue -> (clue.getClue() == -1 ? " " : clue.getClue()) <b>+ "\t"</b> ,
                   false));</pre>

My experience is that you should size the cells: Width: 1.86, Height: 13.5

<h3>4. Attribution</h3>

Much hard work has gone to develop this API. You are free to use it without limit and even create your own user interface version. If you publish a puzzle created by this API publicly on the Internet (intentionally excludes for private use), you should link this  repository.


    


  [1]: https://i.stack.imgur.com/79JJE.png
  [2]: https://i.stack.imgur.com/tNld9.png
  [3]: https://i.stack.imgur.com/5nW7A.png
