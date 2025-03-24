package be.uhasselt.dwi_application.controller.BinManager;

import be.uhasselt.dwi_application.model.workInstruction.picking.PickingBin;
import be.uhasselt.dwi_application.utility.database.repository.pickingBin.BinRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class BinItemController {
    @FXML private Label binIdLabel;
    @FXML private TextField posXField;
    @FXML private TextField posYField;
    @FXML private Button deleteBin_btn;

    private PickingBin bin;
    private PickingBinManagerController managerController;
    private BinRepository binRepository;

    @FXML
    public void initialize() {
        deleteBin_btn.setOnAction(event -> deleteBin());
        binRepository = new BinRepository();
    }

    public void setBin(PickingBin bin, PickingBinManagerController managerController) {
        this.bin = bin;
        this.managerController = managerController;

        binIdLabel.setText("ID: " + bin.getId());
        posXField.setText(String.valueOf(bin.getPos_x()));
        posYField.setText(String.valueOf(bin.getPos_y()));
        posXField.setEditable(false);
        posYField.setEditable(false);
    }

    public void updatePositionFields(int x, int y) {
        posXField.setText(String.valueOf(x));
        posYField.setText(String.valueOf(y));
        bin.setPosition(x, y);
        binRepository.updateBin(bin);
    }

    @FXML
    private void deleteBin() {
        managerController.deleteBin(bin);
    }
}
