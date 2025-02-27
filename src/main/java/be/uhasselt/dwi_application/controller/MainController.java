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
    @FXML private HBox handtracking_hbox;
    @FXML private HBox binManager_hbox;
    @FXML private HBox InstructionManager_hbox;
    @FXML private AnchorPane contentPane;

    private static MainController instance;

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
        if (source == binManager_hbox) {
            loadView(FxmlViews.BIN_MANAGER);
        } else if (source == InstructionManager_hbox ) {
            loadView(FxmlViews.ASSEMBLY_EXPLORER);
        }
    }

    private void loadView(String fxmlPath) {
        try {
            URL fxmlResource = LoadView.class.getResource(fxmlPath);
            if (fxmlResource == null) {
                System.err.println("FXML file not found: " + fxmlPath);
                return;
            }

            Parent newView = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            setContentView(newView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setContentView(Parent view){
        contentPane.getChildren().setAll(view);
    }
}
