package be.uhasselt.dwi_application.controller.WorkInstruction.Assembly;

import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.utility.database.repository.assembly.AssemblyRepository;
import be.uhasselt.dwi_application.utility.FxmlViews;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static be.uhasselt.dwi_application.utility.modules.Dialog.showExceptionDialog;

public class AssemblyManagerController {
    @FXML private TilePane assemblyTilePane;
    @FXML private Button addAssembly_btn;
    @FXML private Button deleteAssembly_btn;
    @FXML private TextField assemblyName_txt;

    private List<Assembly> assemblies;
    private final List<Assembly> selectedAssemblies = new ArrayList<>();
    private final AssemblyRepository assemblyRepository = AssemblyRepository.getInstance();

    @FXML
    public void initialize() {
        try {
            assemblies = assemblyRepository.getAll();
        } catch (Exception e) {
            showExceptionDialog("Error", "Error in AssemblyManagerController.initialize", e);
            e.printStackTrace();
            return;
        }
        populateAssemblyTiles();

        addAssembly_btn.setOnAction(event -> addAssemblyTile());
        deleteAssembly_btn.setOnAction(event -> deleteSelectedTiles());
    }

    private void addAssemblyTile() {
        String assemblyName = assemblyName_txt.getText().trim();
        if (!assemblyName.isEmpty()) {
            Assembly newAssembly = new Assembly(assemblyName);
            assemblyRepository.insert(newAssembly);
            assemblies.add(newAssembly);
            assemblyName_txt.setText("New Assembly");
            populateAssemblyTiles();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a valid assembly");
            alert.showAndWait();
        }
    }

    private void deleteSelectedTiles() {
        if (!selectedAssemblies.isEmpty()) {
            assemblyRepository.deleteAsList(selectedAssemblies);
            assemblies.removeAll(selectedAssemblies);
            selectedAssemblies.clear();
            populateAssemblyTiles();
        }
    }

    private void populateAssemblyTiles() {
        assemblyTilePane.getChildren().clear();
        for (Assembly assembly : assemblies) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(FxmlViews.ASSEMBLY_TILE));
                VBox tile = loader.load();

                AssemblyTileController controller = loader.getController();
                controller.setAssembly(assembly,this::onSelectionChanged);

                assemblyTilePane.getChildren().add(tile);
            } catch (IOException e) {
                e.printStackTrace();
                showExceptionDialog("Error", "Error in AssemblyManagerController.populateAssemblyTiles", e);
            }
        }
    }


    private void onSelectionChanged(Assembly assembly) {
        if (selectedAssemblies.contains(assembly)) {
            selectedAssemblies.remove(assembly);
        } else {
            selectedAssemblies.add(assembly);
        }
    }
}
