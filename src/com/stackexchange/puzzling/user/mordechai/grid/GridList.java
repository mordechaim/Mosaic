package com.stackexchange.puzzling.user.mordechai.grid;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
		return grid.getLength();
	}
	
	@Override
	public T set(int index, T data) {
		T old = get(index);
		grid.set(index, data);
		
		return old;
	}
	
	public void clear() {
		grid.clear();
	}
	
	public boolean isEmpty() {
		return grid.isEmpty();
	}

	
}
