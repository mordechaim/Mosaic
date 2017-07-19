package com.stackexchange.puzzling.user.mordechai.grid;

public interface CellMapper<T> {

	public T map(int x, int y, String str);
}
