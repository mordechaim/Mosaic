package com.stackexchange.puzzling.user.mordechai.grid;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Grid<T> implements Iterable<T> {

	private int x;
	private int y;
	private int width;
	private int height;

	private Matrix<T> matrix;

	private GridList<T> list;
	private RowList<T>[] rowLists;
	private ColumnList<T>[] columnLists;

	@SuppressWarnings("unchecked")
	protected Grid(Matrix<T> matrix, int x, int y, int width, int height) {
		this.matrix = matrix;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		rowLists = new RowList[height];
		columnLists = new ColumnList[width];
	}

	public Grid(int width, int height) {
		this(new Matrix<T>(width, height), 0, 0, width, height);
	}

	public Grid(Grid<? extends T> other) {
		this(other.width, other.height);

		fill((x, y, old) -> other.get(x, y));
	}

	protected Matrix<T> getMatrix() {
		return matrix;
	}

	protected int getX() {
		return x;
	}

	protected int getY() {
		return y;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

	public int length() {
		return width() * height();
	}

	public T get(int x, int y) {
		checkRange(x, y);

		return cellAt(x, y).getData();
	}

	public T get(int linear) {
		checkRange(linear);

		return get(toX(linear), toY(linear));
	}

	public void set(int linear, T data) {
		checkRange(linear);

		set(toX(linear), toY(linear), data);
	}

	public void set(int x, int y, T data) {
		checkRange(x, y);

		cellAt(x, y).setData(data);
	}

	protected Cell<T> cellAt(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height)
			return null;

		x += this.x;
		y += this.y;

		return matrix.cellAt(x, y);
	}

	protected Cell<T> cellAt(int linear) {
		if (linear < 0 || linear >= length())
			return null;

		return cellAt(toX(linear), toY(linear));
	}

	public int indexOf(T data) {
		if (data == null) {
			for (int i = 0; i < length(); i++) {
				if (get(i) == null)
					return i;
			}

			return -1;
		}

		for (int i = 0; i < length(); i++) {
			if (data.equals(get(i)))
				return i;
		}

		return -1;
	}

	protected int indexOfCell(Cell<T> cell) {
		if (cell == null)
			return -1;

		for (int i = 0; i < length(); i++) {
			Cell<T> c = cellAt(i);
			if (c.equals(cell))
				return i;
		}

		return -1;
	}

	protected void checkRange(int x, int y) {
		if (x < 0)
			throw new IndexOutOfBoundsException("x < 0: " + x);
		if (x >= width)
			throw new IndexOutOfBoundsException("x >= width: " + x);
		if (y < 0)
			throw new IndexOutOfBoundsException("y < 0: " + y);
		if (y >= height)
			throw new IndexOutOfBoundsException("y >= height: " + y);
	}

	protected void checkRange(int linear) {
		if (linear < 0)
			throw new IndexOutOfBoundsException("index < 0: " + linear);
		if (linear >= length())
			throw new IndexOutOfBoundsException("index >= length: " + linear);
	}

	public int toX(int linear) {
		checkRange(linear);
		return linear % width();
	}

	public int toY(int linear) {
		checkRange(linear);
		return linear / width();
	}

	public int toLinear(int x, int y) {
		checkRange(x, y);
		return (y * width()) + x;
	}

	public void fill(Supplier<T> supplier) {
		fill((x, y, old) -> supplier.get());
	}

	public void fill(T data) {
		fill((x, y, old) -> data);
	}

	public void replaceAll(Function<T, T> filler) {
		fill((x, y, old) -> filler.apply(old));
	}

	public void fill(CellFunction<T> filler) {
		GridIterator<T> iterator = iterator();
		while (iterator.hasNext()) {
			T old = iterator.next();
			iterator.set(filler.get(iterator.x(), iterator.y(), old));
		}
	}

	public int count(Predicate<T> predicate) {
		int count = 0;
		for (T data : this)
			if (predicate.test(data))
				count++;

		return count;
	}

	public boolean isEmpty() {
		for (T data : this)
			if (data != null)
				return false;

		return true;
	}

	public void clear() {
		fill((T) null);
	}

	public Grid<T> view(int x, int y, int width, int height) {
		if (x < 0)
			throw new IllegalArgumentException(x + ": x < 0");
		if (y < 0)
			throw new IllegalArgumentException(y + ": y < 0");
		if (width < 1)
			throw new IllegalArgumentException(width + ": width < 1");
		if (height < 1)
			throw new IllegalArgumentException(height + ": height < 1");
		if (x + width > width())
			throw new IllegalArgumentException((x + width) + ": x + width > width()");
		if (y + height > height())
			throw new IllegalArgumentException((y + height) + ": y + height  > height()");

		return new Grid<T>(getMatrix(), x + getX(), y + getY(), width, height);
	}

	public Grid<T> getSurroundingCells(int x, int y) {
		int width = 3;
		int height = 3;

		if (x > 0)
			x--;
		else
			width--;

		if (y > 0)
			y--;
		else
			height--;

		while (x + width > width())
			width--;
		while (y + height > height())
			height--; // FIXME

		return view(x, y, width, height);
	}

	public Grid<T> branch(Function<T, T> copier) {
		Grid<T> copy = new Grid<>(width(), height());

		GridIterator<T> iterator = iterator();
		while (iterator.hasNext()) {
			T old = iterator.next();
			copy.set(iterator.x(), iterator.y(), copier.apply(old));
		}

		return copy;
	}

	@Override
	public GridIterator<T> iterator() {
		return new GridIterator<T>(this);
	}

	public List<T> asList() {
		if (list == null)
			list = new GridList<T>(this);

		return list;
	}

	public List<T> row(int row) {
		if (row < 0 || row >= height)
			throw new IndexOutOfBoundsException("row: " + row);

		RowList<T> list = rowLists[row];

		if (list == null) {
			list = new RowList<>(this, row);
			rowLists[row] = list;
		}

		return list;
	}

	public List<T> column(int column) {
		if (column < 0 || column >= width)
			throw new IndexOutOfBoundsException("column: " + column);

		ColumnList<T> list = columnLists[column];

		if (list == null) {
			list = new ColumnList<>(this, column);
			columnLists[column] = list;
		}
		return list;
	}

	public void forEach(CellLocation<T> cell) {
		for (int y = 0; y < height(); y++) {
			for (int x = 0; x < width(); x++) {
				cell.accept(get(x, y), x, y);
			}
		}
	}

	public String toGridString(Function<T, String> generator, boolean gridLines) {
		String line = null;

		GridIterator<T> iterator = iterator();
		if (!iterator.hasNext())
			return "";

		String str = generator.apply(iterator.next());
		StringBuilder builder = new StringBuilder(width() * height() * str.length() * (gridLines ? 2 : 1));

		if (gridLines) {
			line = String.join("", Collections.nCopies(str.length(), "-"));
			line = String.join("", Collections.nCopies(width(), line + "+"));
			line = "\n" + line.substring(0, line.length() - 1);
		}

		while (iterator.hasNext()) {
			builder.append(str);
			if (gridLines) {
				if (!iterator.isRowEnd()) {
					builder.append("|");
				} else if (!iterator.isColumnEnd()) {
					builder.append(line);
				}
			}
			if (iterator.isRowEnd()) {
				builder.append("\n");
			}
			str = generator.apply(iterator.next());
		}

		builder.append(str);

		return builder.toString();
	}

	public String toGridString(Function<T, String> generator) {
		return toGridString(generator, false);
	}

	public String toGridString(Function<T, String> generator, String delimiter) {
		StringBuilder builder = new StringBuilder();
		GridIterator<T> iterator = iterator();

		while (iterator.hasNext()) {
			builder.append(generator.apply(iterator.next()));
			if (!iterator.isRowEnd())
				builder.append(delimiter);
			else if (!iterator.isColumnEnd()) // prevents newline on last element
				builder.append("\n");
		}

		return builder.toString();
	}

	@Override
	public String toString() {
		return toGridString(t -> {
			if (t == null)
				return "null";

			String str = t.toString().replace("\"", "\"\"");
			if (str.contains(",")) {
				str = "\"" + str + "\"";
			}

			return str;
		}, ",");
	}

	public static <E> Grid<E> fromString(String str, String delimiter, CellMapper<E> mapper) {
		String lines[] = str.split("\\r?\\n", -1);

		if (lines.length == 0)
			return null;

		Grid<E> grid = null;

		String[] tokens;
		for (int y = 0; y < lines.length; y++) {
			tokens = lines[y].split(delimiter);
			for (int x = 0; x < tokens.length; x++) {
				if (grid == null) {
					grid = new Grid<>(tokens.length, lines.length);
				} else if (tokens.length > grid.width()) {
					Grid<E> newGrid = new Grid<>(tokens.length, lines.length);
					grid.copyTo(newGrid);

					grid = newGrid;
				}
				grid.set(x, y, mapper.map(x, y, tokens[x]));
			}
		}

		return grid;
	}

	public static <E> Grid<E> fromString(String str, LineTokenizer tokenizer, CellMapper<E> mapper) {
		String lines[] = str.split("\\r?\\n", -1);

		if (lines.length == 0)
			return null;

		Grid<E> grid = null;

		List<String> tokens;
		for (int y = 0; y < lines.length; y++) {
			tokens = tokenizer.tokenize(lines[y]);
			for (int x = 0; x < tokens.size(); x++) {
				if (grid == null) {
					grid = new Grid<>(tokens.size(), lines.length);
				} else if (tokens.size() > grid.width()) {
					Grid<E> newGrid = new Grid<>(tokens.size(), lines.length);
					grid.copyTo(newGrid);

					grid = newGrid;
				}
				grid.set(x, y, mapper.map(x, y, tokens.get(x)));
			}
		}

		return grid;
	}

	/*
	 * https://stackoverflow.com/a/13655640/1751640
	 */
	private static List<String> parseLineAsCSV(String str) {
		
		try (Reader r = new StringReader(str)) {

			int ch = r.read();
			while (ch == '\r') {
				// ignore linefeed chars wherever, particularly just before end of file
				ch = r.read();
			}
			if (ch < 0) {
				return null;
			}
			List<String> store = new ArrayList<String>();
			StringBuilder curVal = new StringBuilder();
			boolean inquotes = false;
			boolean started = false;
			while (ch >= 0) {
				if (inquotes) {
					started = true;
					if (ch == '\"') {
						inquotes = false;
					} else {
						curVal.append((char) ch);
					}
				} else {
					if (ch == '\"') {
						inquotes = true;
						if (started) {
							// if this is the second quote in a value, add a quote
							// this is for the double quote in the middle of a value
							curVal.append('\"');
						}
					} else if (ch == ',') {
						store.add(curVal.toString());
						curVal.setLength(0);
						started = false;
					} else if (ch == '\r') {
						// ignore LF characters
					} else if (ch == '\n') {
						// end of a line, break out
						break;
					} else {
						curVal.append((char) ch);
					}
				}
				ch = r.read();
			}
			store.add(curVal.toString());
			return store;
			
		} catch (IOException e) {
			return Collections.emptyList();
		}
	}

	public static <E> Grid<E> fromString(String str, CellMapper<E> mapper) {
		return fromString(str, Grid::parseLineAsCSV, mapper);
	}

	public void copyTo(Grid<? super T> other, int xOffset, int yOffset) {
		if (xOffset < 0)
			throw new IllegalArgumentException("x offset < 0: " + xOffset);
		if (yOffset < 0)
			throw new IllegalArgumentException("y offset < 0: " + yOffset);
		if (other.width() + xOffset < width())
			throw new IndexOutOfBoundsException("Width: " + other.width());
		if (other.height() + yOffset < height())
			throw new IndexOutOfBoundsException("Height: " + other.height());

		GridIterator<T> iterator = iterator();
		while (iterator.hasNext()) {
			T item = iterator.next();
			other.set(iterator.x() + xOffset, iterator.y() + yOffset, item);
		}
	}

	public void copyTo(Grid<? super T> other) {
		copyTo(other, 0, 0);
	}
}
