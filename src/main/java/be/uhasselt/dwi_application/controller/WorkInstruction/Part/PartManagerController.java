package be.uhasselt.dwi_application.controller.WorkInstruction.Part;

import be.uhasselt.dwi_application.controller.WorkInstruction.InstructionManagerController;
import be.uhasselt.dwi_application.model.picking.Part;
import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.utility.FxmlViews;
import be.uhasselt.dwi_application.utility.database.repository.assembly.AssemblyRepository;
import be.uhasselt.dwi_application.utility.database.repository.part.PartRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static be.uhasselt.dwi_application.utility.modules.Dialog.showErrorDialog;
import static be.uhasselt.dwi_application.utility.modules.Dialog.showExceptionDialog;

public class PartManagerController {
    @FXML private VBox PartContainer_vbox;
    @FXML private Button addPart_btn;
    @FXML private Button deleteSelectedParts_btn;

    private Assembly assembly;
    private final PartRepository partRepository = PartRepository.getInstance();
    private static PartManagerController instance;
    private final Set<Part> selectedParts = new HashSet<>();

    public PartManagerController(Assembly assembly) {
        this.assembly = assembly;
        instance = this;
    }

    public static PartManagerController getInstance(){
        return instance;
    }

    @FXML
    public void initialize() {
        addPart_btn.setOnAction(_ -> addPart() );
        deleteSelectedParts_btn.setOnAction(_ -> deleteSelectedParts());
        populateParts();
    }

    public void addSelectedPart(Part part) {
        selectedParts.add(part);
    }

    public void removeSelectedPart(Part part) {
        selectedParts.remove(part);
    }


    private void deleteSelectedParts() {
        if (selectedParts.isEmpty()) {
            showErrorDialog("No Selection", "No parts selected", "Please select parts to delete.", "OK");
            return;
        }

        try {
            System.out.println("Deleting " + selectedParts.size() + " parts");
            partRepository.deleteAsCollection(selectedParts);
            selectedParts.clear();
            populateParts();
            InstructionManagerController.getInstance().populatePartSelector();
        } catch (Exception e) {
            showErrorDialog("Delete Failed", "", e.getMessage(), "Close");
        }
    }

    private void addPart() {
        Part newPart = new Part("New Part", assembly);
        partRepository.add(newPart);

        populateParts();
        InstructionManagerController.getInstance().populatePartSelector();
    }

    public void populateParts() {
        PartContainer_vbox.getChildren().clear();
        List<Part> parts = assembly.getAllParts();

        for (Part part : parts) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(FxmlViews.PART_ITEM));

                PartItemController controller = new PartItemController(assembly, part);
                loader.setControllerFactory(_ -> controller);

                AnchorPane partPane = loader.load();
                PartContainer_vbox.getChildren().add(partPane);
            } catch (IOException e) {
                e.printStackTrace();
                showExceptionDialog("Error loading Part", e);
            }
        }
    }

}
