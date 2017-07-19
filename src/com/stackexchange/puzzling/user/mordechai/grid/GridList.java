package com.stackexchange.puzzling.user.mordechai.grid;

import java.util.AbstractList;

public class GridList<T> extends AbstractList<T> {
	
	private Grid<T> grid;
	
	public GridList(Grid<T> grid) {
		this.grid = grid;
	}

	@Override
	public T get(int index) {
		return grid.get(index);
	}

	@Override
	public int size() {
		return grid.length();
	}
	
	@Override
	public T set(int index, T data) {
		T old = get(index);
		grid.set(index, data);
		
		return old;
	}
	
	@Override
	public void clear() {
		grid.clear();
	}
	
	@Override
	public boolean isEmpty() {
		return grid.isEmpty();
	}

	
}
