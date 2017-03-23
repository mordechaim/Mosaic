package com.stackexchange.puzzling.user.mordechai.grid;

public class Cell<T> {

	private T data;
	private int x, y;

	public Cell(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

}
