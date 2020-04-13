public enum PANE_REF {
	MAIN_PANE("main.fxml"),
	CHART_PANE("chart.fxml");

	private final String pane;
	PANE_REF(String s) {
		this.pane = s;
	}

	public String getPane() {
		return pane;
	}
}
