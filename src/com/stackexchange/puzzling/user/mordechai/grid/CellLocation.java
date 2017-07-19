package com.stackexchange.puzzling.user.mordechai.grid;

public interface CellLocation<T> {
	void accept(T data, int x, int y);
}
