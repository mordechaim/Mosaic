package com.stackexchange.puzzling.user.mordechai.grid;

import java.util.AbstractList;

public class ColumnList<T> extends AbstractList<T> {

	private Grid<T> grid;
	private int x;

	public ColumnList(Grid<T> grid, int x) {
		this.grid = grid;
		this.x = x;
	}

	@Override
	public T get(int y) {
		return grid.get(x, y);
	}

	@Override
	public int size() {
		return grid.height();
	}

	@Override
	public T set(int y, T data) {
		T old = get(y);
		grid.set(x, y, data);

		return old;
	}

	@Override
	public void clear() {
		for (int i = 0; i < grid.width(); i++) {
			set(i, null);
		}
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < grid.width(); i++) {
			if(get(i) != null)
				return false;
		}
		
		return true;
	}
}
