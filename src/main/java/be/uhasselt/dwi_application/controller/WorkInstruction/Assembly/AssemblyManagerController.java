package be.uhasselt.dwi_application.controller.WorkInstruction.Assembly;

import be.uhasselt.dwi_application.controller.Controller;
import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.utility.database.repository.assembly.AssemblyRepository;
import be.uhasselt.dwi_application.utility.FxmlViews;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static be.uhasselt.dwi_application.utility.modules.Dialog.showExceptionDialog;

public class AssemblyManagerController implements Controller {
    @FXML private TilePane assemblyTilePane;
    @FXML private Button addAssembly_btn;
    @FXML private Button deleteAssembly_btn;
    @FXML private TextField assemblyName_txt;
    @FXML private ColorPicker colorPicker;

    private List<Assembly> assemblies;
    private final List<Assembly> selectedAssemblies = new ArrayList<>();
    private final AssemblyRepository assemblyRepository = AssemblyRepository.getInstance();
    private final Map<Assembly, AssemblyTileController> tileControllerMap = new HashMap<>();

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

        addAssembly_btn.setOnAction(_ -> addAssemblyTile());
        deleteAssembly_btn.setOnAction(_ -> deleteSelectedTiles());
        colorPicker.setOnAction(_ -> setColorToSelectedAssemblies());
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

    private void setColorToSelectedAssemblies() {
        Color selectedColor = colorPicker.getValue();
        for (Assembly assembly : selectedAssemblies) {
            assembly.setColor(selectedColor); // Assuming setColor() method exists in Assembly
            assemblyRepository.update(assembly); // Save the updated color in DB

            if (tileControllerMap.containsKey(assembly)) {
                tileControllerMap.get(assembly).updateColor(selectedColor);
            }
        }
    }

    private void populateAssemblyTiles() {
        assemblyTilePane.getChildren().clear();
        for (Assembly assembly : assemblies) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(FxmlViews.ASSEMBLY_TILE));
                AssemblyTileController controller = new AssemblyTileController(assembly, this::onSelectionChanged);
                loader.setControllerFactory(_ -> controller);

                VBox tile = loader.load();
                tileControllerMap.put(assembly, controller);

                assemblyTilePane.getChildren().add(tile);
            } catch (IOException e) {
                e.printStackTrace();
                showExceptionDialog("Error", "Error in AssemblyManagerController.populateAssemblyTiles", e);
            }
        }
    }

    private void onSelectionChanged(Assembly assembly) {
        if (selectedAssemblies.contains(assembly)) {
            System.out.println("removing");
            selectedAssemblies.remove(assembly);
        } else {
            System.out.println("adding");
            selectedAssemblies.add(assembly);
        }
        System.out.println(selectedAssemblies);
    }


    @Override
    public void cleanup() {

    }
}
