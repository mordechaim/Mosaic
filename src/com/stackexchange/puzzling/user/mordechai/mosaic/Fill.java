package com.stackexchange.puzzling.user.mordechai.mosaic;

public enum Fill {

	FILLED("#"), EMPTY(" "), X("X");
	
	private String string;
	
	Fill(String str) {
		this.string = str;
	}
	
	@Override
	public String toString() {
		return string;
	}
}
