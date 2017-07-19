package com.stackexchange.puzzling.user.mordechai.grid;

import java.util.AbstractList;

public class RowList<T> extends AbstractList<T> {

	private Grid<T> grid;
	private int y;

	public RowList(Grid<T> grid, int y) {
		this.grid = grid;
		this.y = y;
	}

	@Override
	public T get(int x) {
		return grid.get(x, y);
	}

	@Override
	public int size() {
		return grid.height();
	}

	@Override
	public T set(int x, T data) {
		T old = get(x);
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
