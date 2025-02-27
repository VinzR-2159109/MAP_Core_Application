package be.uhasselt.dwi_application.controller.WorkInstruction.Part;

import be.uhasselt.dwi_application.controller.Controller;
import be.uhasselt.dwi_application.controller.WorkInstruction.InstructionManagerController;
import be.uhasselt.dwi_application.model.picking.Part;
import be.uhasselt.dwi_application.model.picking.PickingBin;
import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.utility.database.repository.assembly.AssemblyRepository;
import be.uhasselt.dwi_application.utility.database.repository.part.PartRepository;
import be.uhasselt.dwi_application.utility.database.repository.pickingBin.BinRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

import static be.uhasselt.dwi_application.utility.modules.Dialog.showErrorDialog;

public class PartItemController implements Controller {
    @FXML private Label partName_lbl;
    @FXML private TextField partName_txt;
    @FXML private Button editSave_btn;
    @FXML private ComboBox<PickingBin> binNum_cbx;
    @FXML private CheckBox partCheckbox;

    private Part part;
    private Assembly assembly;

    private final AssemblyRepository assemblyRepository;
    private final PartRepository partRepository;
    private final BinRepository binRepository;

    public PartItemController(Assembly assembly, Part part) {
        this.assembly = assembly;
        this.part = part;

        this.assemblyRepository = AssemblyRepository.getInstance();;
        this.partRepository = PartRepository.getInstance();
        this.binRepository = BinRepository.getInstance();
    }

    @FXML
    public void initialize() {
        partName_txt.setVisible(false);
        partName_lbl.setVisible(true);
        editSave_btn.setText("Edit");

        editSave_btn.setOnMouseClicked(_ -> toggleEditSave());
        binNum_cbx.setOnAction(_ -> updatePartBin());
        partCheckbox.setOnAction(_ -> toggleSelection());

        if (part == null) {
            showErrorDialog("Error", "Part is null", "Check PartItemController.initialize()", "Close");
            return;
        }
        partName_lbl.setText(part.getName());
        partName_txt.setText(part.getName());

        List<PickingBin> assignedBins = binRepository.getBinsByPartId(part.getId());
        if (!assignedBins.isEmpty()) {
            binNum_cbx.setValue(assignedBins.getFirst());
        }

        populateBinComboBox();
    }

    private void toggleSelection() {
        if (partCheckbox.isSelected()) {
            PartManagerController.getInstance().addSelectedPart(part);
        } else {
            PartManagerController.getInstance().removeSelectedPart(part);
        }
    }

    private void toggleEditSave() {
        boolean isEditing = partName_txt.isVisible();

        if (isEditing) {
            savePartName();
        } else {
            partName_lbl.setVisible(false);
            partName_txt.setVisible(true);
            partName_txt.requestFocus();
            editSave_btn.setText("Save");
        }
    }

    private void savePartName() {
        if (part == null) {
            showErrorDialog("Error", "No Part Selected", "Select a part before renaming.", "OK");
            return;
        }

        String newName = partName_txt.getText().trim();
        if (!newName.isEmpty() && !newName.equals(part.getName())) {
            part.setName(newName);
            partRepository.update(part); // Save to database
        }

        // Update UI
        partName_lbl.setText(part.getName());
        partName_lbl.setVisible(true);
        partName_txt.setVisible(false);
        editSave_btn.setText("Edit");

        // Refresh part selector in instruction manager
        InstructionManagerController.getInstance().populatePartSelector();

        if (assembly != null) {
            assemblyRepository.updateAssembly(assembly);
        }
    }

    private void populateBinComboBox() {
        binNum_cbx.getItems().addAll(binRepository.getAll());
    }

    private void updatePartBin() {
        if (part == null) {
            return; // No part selected, nothing to update
        }

        PickingBin selectedBin = binNum_cbx.getSelectionModel().getSelectedItem();
        if (selectedBin == null) {
            return; // No bin selected, nothing to update
        }

        List<PickingBin> binsWithPart = binRepository.getBinsByPartId(part.getId());

        // Remove part from previous bins
        for (PickingBin bin : binsWithPart) {
            if (!bin.getId().equals(selectedBin.getId())) {
                bin.removePart();
                binRepository.updateBin(bin);
            }
        }

        // Assign part to new bin
        selectedBin.setPart(part);
        binRepository.updateBin(selectedBin);
    }

    @Override
    public void cleanup() {

    }
}
