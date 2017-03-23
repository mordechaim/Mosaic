package com.stackexchange.puzzling.user.mordechai.grid;

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

		rowLists = (RowList<T>[]) new RowList[height];
		columnLists = (ColumnList<T>[]) new ColumnList[width];
	}

	public Grid(int width, int height) {
		this(new Matrix<T>(width, height), 0, 0, width, height);
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

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getLength() {
		return getWidth() * getHeight();
	}

	public T get(int x, int y) {
		if (x < 0 || x >= width)
			throw new IndexOutOfBoundsException("x: " + x);
		if (y < 0 || y >= height)
			throw new IndexOutOfBoundsException("y: " + y);

		return cellAt(x, y).getData();
	}

	public T get(int linear) {
		if (linear < 0 || linear >= getLength())
			throw new IndexOutOfBoundsException("index " + linear);

		return get(linear % getWidth(), linear / getWidth());
	}

	public void set(int linear, T data) {
		if (linear < 0 || linear >= getLength())
			throw new IndexOutOfBoundsException("index" + linear);

		set(linear % getWidth(), linear / getWidth(), data);
	}

	public void set(int x, int y, T data) {
		if (x < 0 || x >= width)
			throw new IndexOutOfBoundsException("x: " + x);
		if (y < 0 || y >= height)
			throw new IndexOutOfBoundsException("y: " + y);

		cellAt(x, y).setData(data);
	}

	public Cell<T> cellAt(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height)
			return null;

		x += this.x;
		y += this.y;

		return matrix.cellAt(x, y);
	}

	public Cell<T> cellAt(int linear) {
		if (linear < 0 || linear >= getLength())
			return null;

		return cellAt(linear % getWidth(), linear / getWidth());
	}

	public int indexOf(T data) {
		if (data == null) {
			for (int i = 0; i < getLength(); i++)
				if (get(i) == null)
					return i;
		}

		for (int i = 0; i < getLength(); i++) {
			T t = get(i);
			if (t != null && t.equals(data))
				return i;
		}

		return -1;
	}

	public int indexOfCell(Cell<T> cell) {
		if (cell == null)
			return -1;

		for (int i = 0; i < getLength(); i++) {
			Cell<T> c = cellAt(i);
			if (c.equals(cell))
				return i;
		}

		return -1;
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

	public void fill(CellFiller<T> filler) {
		GridIterator<T> iterator = iterator();
		while (iterator.hasNext()) {
			T old = iterator.next();
			iterator.set(filler.get(iterator.getX(), iterator.getY(), old));
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
		if (x + width > getWidth())
			throw new IllegalArgumentException((x + width) + ": x + width > getWidth()");
		if (y + height > getHeight())
			throw new IllegalArgumentException((y + height) + ": y + height  > getHeight()");

		return new Grid<T>(getMatrix(), x + getX(), y + getY(), width, height);
	}

	public Grid<T> branch(Function<T, T> copier) {
		Grid<T> copy = new Grid<>(getWidth(), getHeight());

		GridIterator<T> iterator = iterator();
		while (iterator.hasNext()) {
			T old = iterator.next();
			copy.set(iterator.getX(), iterator.getY(), copier.apply(old));
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

	public String toGridString(Function<T, String> generator, boolean gridLines) {
		String line = null;

		GridIterator<T> iterator = iterator();
		if (!iterator.hasNext())
			return "";

		String str = generator.apply(iterator.next());
		StringBuilder builder = new StringBuilder(getWidth() * getHeight() * str.length() * (gridLines ? 2 : 1));

		if (gridLines) {
			line = String.join("", Collections.nCopies(str.length(), "-"));
			line = String.join("", Collections.nCopies(getWidth(), line + "+"));
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

		builder.append(str + "\n");

		return builder.toString();
	}
}
