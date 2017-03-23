package com.stackexchange.puzzling.user.mordechai.mosaic.solvers;

public enum StandardState implements State {
	INITIALIZING, READY, RUNNING, PAUSED, CANCELLED, FAILED, SUCCEEDED;
}
