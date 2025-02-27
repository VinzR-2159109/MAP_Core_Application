package be.uhasselt.dwi_application;

import be.uhasselt.dwi_application.utility.FxmlViews;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class DWI_Core_Application extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(DWI_Core_Application.class.getResource(FxmlViews.MAIN_VIEW));

        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("DWI Application");
        stage.setScene(scene);
        stage.show();

        stage.setMaximized(true);
    }

    public static void main(String[] args) {
        launch();
    }
}