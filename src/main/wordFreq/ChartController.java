import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ChartController implements Initializable {

	@FXML
	private BarChart<String, Number> freqChart;

	@Override public void initialize(URL location, ResourceBundle resources) {

		freqChart.applyCss();
		XYChart.Series<String, Number> series = new XYChart.Series<>();

		WordFreqProcessor.ProcessorState.getState().getProcessResult()
				.stream()
				.sorted((e1,e2) -> e2.getFrequency() - e1.getFrequency())
				.limit(WordFreqProcessor.ProcessorState.getState().getProcessorInput().getResultsNo())
				.forEach(entry -> series.getData().addAll(new XYChart.Data<>(entry.getWord(),entry.getFrequency())));

		freqChart.getData().add(series);
	}

	@FXML
	public void backAction(ActionEvent actionEvent) {
		WordFreqProcessor.ProcessorState.getState().setProcessResult(null);
		Node node = (Node) actionEvent.getSource();
		FXUtils.loadScene((Stage) node.getScene().getWindow(), PANE_REF.MAIN_PANE, getClass().getClassLoader(),false);
	}
}
