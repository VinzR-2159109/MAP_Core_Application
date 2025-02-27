package be.uhasselt.dwi_application.utility;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class LoadView {
    public static void loadView(String fxmlPath, MouseEvent event) {
        try {
            URL fxmlResource = LoadView.class.getResource(fxmlPath);
            if (fxmlResource == null) {
                System.err.println("FXML file not found: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlResource);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setFullScreen(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static FXMLLoader getFXMLLoader(String fxmlPath) {
        URL fxmlResource = LoadView.class.getResource(fxmlPath);
        if (fxmlResource == null) {
            throw new IllegalArgumentException("FXML file not found: " + fxmlPath);
        }

        return new FXMLLoader(fxmlResource);
    }
}
