package com.stackexchange.puzzling.user.mordechai.grid;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class GridIterator<T> implements Iterator<T> {

	private int x = -1;
	private int y = -1;

	private Grid<T> grid;

	private boolean byRow;
	private boolean wrapping;

	public GridIterator(Grid<T> grid, boolean byRow, boolean wrapping) {
		this.grid = grid;
		setByRow(byRow);
		setWrapping(wrapping);
	}

	public GridIterator(Grid<T> grid) {
		this(grid, true, false);
	}

	public boolean hasUp() {
		return y > 0;
	}

	public boolean hasRight() {
		return x < grid.getWidth() - 1;
	}

	public boolean hasDown() {
		return y < grid.getHeight() - 1;
	}

	public boolean hasLeft() {
		return x > 0;
	}

	public boolean hasNextRow() {
		return hasDown();
	}

	public boolean hasNextColumn() {
		return hasRight();
	}

	public boolean hasPreviousRow() {
		return hasUp();
	}

	public boolean hasPreviousColumn() {
		return hasLeft();
	}

	public boolean isRowStart() {
		return !hasLeft();
	}

	public boolean isRowEnd() {
		return !hasRight();
	}

	public boolean isColumnStart() {
		return !hasUp();
	}

	public boolean isColumnEnd() {
		return !hasDown();
	}

	@Override
	public boolean hasNext() {
		return hasRight() || hasNextRow();
	}

	public boolean hasPrevious() {
		return hasLeft() || hasPreviousRow();
	}

	public T get() {
		return getCell().getData();
	}

	public void set(T data) {
		getCell().setData(data);
	}

	public Cell<T> getCell() {
		if (x < 0)
			x++;
		if( y < 0)
			y++;

		return grid.cellAt(x, y);
	}

	public T up() {
		if (!hasUp()) {
			if (isWrapping())
				return columnEnd();
			throw new NoSuchElementException();
		}

		y--;
		return get();
	}

	public T right() {
		if (!hasRight()) {
			if (isWrapping())
				return rowStart();
			throw new NoSuchElementException();
		}

		x++;
		return get();
	}

	public T down() {
		if (!hasDown()) {
			if (isWrapping())
				return columnStart();
			throw new NoSuchElementException();
		}
		
		y++;
		return get();
	}

	public T left() {
		if (!hasLeft()) {
			if (isWrapping())
				return rowEnd();
			throw new NoSuchElementException();
		}

		x--;
		return get();
	}

	public T nextRow() {
		if (!hasNextRow())
			throw new NoSuchElementException();

		x = 0;
		y++;

		return get();

	}

	public T previousRow() {
		if (!hasPreviousRow())
			throw new NoSuchElementException();

		x = 0;
		y--;

		return get();
	}

	public T previousRowEnd() {
		if (!hasPreviousRow())
			throw new NoSuchElementException();

		x = grid.getWidth();
		y--;

		return get();
	}

	public T nextColumn() {
		if (!hasNextColumn())
			throw new NoSuchElementException();

		y = 0;
		x++;

		return get();
	}

	public T previousColumn() {
		if (!hasPreviousColumn())
			throw new NoSuchElementException();

		y = 0;
		x--;

		return get();
	}

	public T previousColumnEnd() {
		if (!hasPreviousColumn())
			throw new NoSuchElementException();

		y = grid.getHeight();
		x--;

		return get();
	}

	public T rowEnd() {
		x = grid.getWidth();
		return get();
	}

	public T rowStart() {
		x = 0;
		return get();
	}

	public T columnEnd() {
		y = grid.getHeight();
		return get();
	}

	public T columnStart() {
		y = 0;
		return get();
	}

	public T jump(int x, int y) {
		if (x < 0 || x >= grid.getWidth())
			throw new IndexOutOfBoundsException("x: " + x);
		if (y < 0 || y >= grid.getHeight())
			throw new IndexOutOfBoundsException("y: " + y);

		this.x = x;
		this.y = y;

		return get();
	}

	public T jumpRow(int y) {
		return jump(this.x, y);
	}

	public T jumpColumn(int x) {
		return jump(x, this.y);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public T next() {
		if (byRow) {
			if (hasRight())
				return right();
			return nextRow();
		} else {
			if (hasDown())
				return down();
			return nextColumn();
		}
	}

	public T previous() {
		if (byRow) {
			if (hasLeft())
				return left();
			return previousRowEnd();
		} else {
			if (hasUp())
				return up();
			return previousColumnEnd();
		}
	}

	public boolean isByRow() {
		return byRow;
	}

	public void setByRow(boolean b) {
		byRow = b;
	}

	public boolean isWrapping() {
		return wrapping;
	}

	public void setWrapping(boolean b) {
		wrapping = b;
	}

}
