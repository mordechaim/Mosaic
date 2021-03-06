package com.stackexchange.puzzling.user.mordechai.mosaic.fx;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;

public class Bottom extends TabPane {

	private static final Global global = Global.getInstance();

	public Bottom() {
		setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

		EditPane editPane = new EditPane();
		GeneratePane generatePane = new GeneratePane();
		SolvePane solvePane = new SolvePane();

		Tab editTab = new Tab("Edit", editPane);
		Tab generateTab = new Tab("Generate", generatePane);
		Tab solveTab = new Tab("Solve", solvePane);

		editTab.setTooltip(new Tooltip("Change elements of the mosaic"));
		generateTab.setTooltip(new Tooltip("Generate a valid puzzle with\nunderlying image data"));
		solveTab.setTooltip(new Tooltip("Let the machine solve the mosaic"));

		editTab.disableProperty().bind(global.autoSolvingProperty().or(global.generatingProperty()));
		generateTab.disableProperty().bind(global.autoSolvingProperty());
		solveTab.disableProperty().bind(global.generatingProperty());

		getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
			if(global.getMosaicPane() == null)
				return;
			
			if (ov == editTab) {
				editPane.liftToggles();
			} else if (ov == generateTab) {
				global.getMosaicPane().setEditor(EditorType.NONE);
			} else if (ov == solveTab) {
				solvePane.cancel();
			}

			if (nv == generateTab) {
				global.getMosaicPane().setEditor(EditorType.CLUE);
			}

			global.getMosaicPane().setMouseTransparent(nv == generateTab);
		});

		getTabs().addAll(editTab, generateTab, solveTab);

		setTabMinWidth(75);
		setMinHeight(100);
	}

}
