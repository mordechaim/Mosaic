package com.stackexchange.puzzling.user.mordechai.grid;

public interface CellFunction<T> {

	T get(int x, int y, T old);
}
