import animatefx.animation.Bounce;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class MainController implements Initializable {

	@FXML
	public JFXTextField countNo;

	@FXML
	private JFXTextArea excludedWords;

	@FXML
	private JFXRadioButton excludeSmall;

	@FXML
	private JFXButton processBtn;

	@FXML
	private Circle circle1;

	@FXML
	private Circle circle2;

	@FXML
	private Circle circle3;

	private boolean exclSmall = false;

	@Override public void initialize(URL location, ResourceBundle resources) {
		excludeSmall.getStyleClass().add("-jfx-selected-color: #2196f3!important;");
		excludeSmall.setOnAction(event -> exclSmall = excludeSmall.isSelected());
		processBtn.setOnAction(event -> {
			loadingAnimation(true);
			new Thread(() -> {
				ProcessResult processResult = process();
				Platform.runLater(new Runnable() {
					@Override public void run() {
						loadingAnimation(false);
						if (processResult.isError) {
							FXUtils.errorBox(processResult.message,"","ERROR");
						} else if (!processResult.processed) {
							FXUtils.info(processResult.message,"","INFO");
						} else {
							Node node = (Node) event.getSource();
							FXUtils.loadScene((Stage) node.getScene().getWindow(), PANE_REF.CHART_PANE,
									getClass().getClassLoader(),true);
						}
					}
				});
			}).start();
		});
	}

	private void loadingAnimation(boolean enabled) {
		processBtn.setVisible(!enabled);
		circle1.setVisible(enabled);
		circle2.setVisible(enabled);
		circle3.setVisible(enabled);
		new Bounce(circle1).setCycleCount(Integer.MAX_VALUE).setDelay(Duration.valueOf("200ms")).play();
		new Bounce(circle2).setCycleCount(Integer.MAX_VALUE).setDelay(Duration.valueOf("400ms")).play();
		new Bounce(circle3).setCycleCount(Integer.MAX_VALUE).setDelay(Duration.valueOf("600ms")).play();
	}

	private ProcessResult process() {
		ProcessorInput processorInput = getProcessorInput();
		if (processorInput == null) return new ProcessResult("Could not determine input", false, true);
		WordFreqProcessor wordFreqProcessor = new WordFreqProcessor(processorInput);
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		AtomicReference<String> err = new AtomicReference<>();
		executorService.execute(() -> {
			try {
				wordFreqProcessor.process();
			} catch (Exception e) {
				err.set(e.getMessage());
				e.printStackTrace();
			}
		});
		executorService.shutdown();
		try {
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			return new ProcessResult("Processing thread was interrupted", false, true);
		}
		if(executorService.isShutdown()) {
			if (WordFreqProcessor.ProcessorState.getState().getProcessResult().isEmpty()) {
				if (err.get() == null)
					return new ProcessResult("Nothing found :( Make sure to put .txt/.doc/.docx documents in this directory", false, false);
				else {
					FXUtils.errorBox(err.get(),"","Failed to process");
					loadingAnimation(false);
				}
			} else {
				return new ProcessResult(null, true , false);
			}
		}
		return new ProcessResult("Something very bad happened", false,false);
	}

	private static class ProcessResult {
		String message;
		boolean processed;
		boolean isError;

		public ProcessResult(String message, boolean processed, boolean isError) {
			this.message = message;
			this.processed = processed;
			this.isError = isError;
		}
	}

	private ProcessorInput getProcessorInput() {
		String[] excluded = new String[0x00];
		if(excludedWords.getText() != null) {
			excluded = excludedWords.getText().trim().split(",");
		}
		String countNoText = countNo.getText();
		int resultsNo = 10;
		if (countNoText != null && !countNoText.trim().isEmpty()) {
			if(isInteger(countNoText)) {
				try {
					resultsNo = Integer.parseInt(countNoText);
				} catch (NumberFormatException e) {
					return null;
				}
			} else {
				return null;
			}
		}
		return new ProcessorInput(FileUtils.getAllFiles(new File(".")), excluded, exclSmall, resultsNo);
	}

	private static boolean isInteger(String str) {
		if (str == null) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}
}
