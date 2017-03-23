package com.stackexchange.puzzling.user.mordechai.grid;

public interface CellFiller<T> {
	T get(int x, int y, T old);
}
