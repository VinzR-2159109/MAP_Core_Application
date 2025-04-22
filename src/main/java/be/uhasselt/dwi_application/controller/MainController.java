package be.uhasselt.dwi_application.controller;

import be.uhasselt.dwi_application.utility.FxmlViews;
import be.uhasselt.dwi_application.utility.LoadView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class MainController {
    @FXML private HBox InstructionManager_hbox;
    @FXML private HBox Settings_hbox;
    @FXML private AnchorPane contentPane;

    private static MainController instance;
    private Controller currentController;

    @FXML
    private void initialize() {
        instance = this;
    }

    @FXML
    public static MainController getInstance() {
        return instance;
    }

    public void onViewClicked(MouseEvent mouseEvent) {
        HBox source = (HBox) mouseEvent.getSource();
        if (source == InstructionManager_hbox ) {
            loadView(FxmlViews.ASSEMBLY_EXPLORER);
        } else if (source == Settings_hbox){
            loadView(FxmlViews.SETTINGS);
        }
    }

    private void loadView(String fxmlPath) {
        try {
            URL fxmlResource = LoadView.class.getResource(fxmlPath);
            if (fxmlResource == null) {
                System.err.println("FXML file not found: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlResource);
            Parent newView = loader.load();
            Object controller = loader.getController();

            setContentView(newView, controller);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setContentView(Parent view, Object controller) {
        // Cleanup previous controller
        if (currentController instanceof Controller cleanupController) {
            cleanupController.cleanup();
        }

        // Store new controller
        if (controller instanceof Controller) {
            currentController = (Controller) controller;
        }

        // Replace content
        contentPane.getChildren().setAll(view);
    }
}
