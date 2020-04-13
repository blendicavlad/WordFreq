import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class FXUtils {

	public static void setPane(Pane oldPane, Pane newPane) {
		oldPane.setManaged(false);
		oldPane.getChildren().clear();
		oldPane.getChildren().add(newPane);
	}

	public static void loadScene(Stage currentStage, PANE_REF pane, ClassLoader classLoader, boolean resizeable) {
		try {
			Parent parent = FXMLLoader.load(Objects.requireNonNull(classLoader.getResource(pane.getPane())));
			if (parent != null) {
				Scene scene = new Scene(parent);
				currentStage.close();
				currentStage.setScene(scene);
				currentStage.setResizable(resizeable);
				currentStage.show();
			}
		} catch (IOException e) {
			errorBox("Could not find: " + pane.getPane() ,"","Resource not found!");
		}
	}

	public static void errorBox(String infoMessage, String headerText, String title){
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setContentText(infoMessage);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.showAndWait();
	}

	public static void info(String infoMessage, String headerText, String title){
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setContentText(infoMessage);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.showAndWait();
	}
}
