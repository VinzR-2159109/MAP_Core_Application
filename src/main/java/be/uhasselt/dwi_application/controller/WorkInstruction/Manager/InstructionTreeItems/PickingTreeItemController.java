package be.uhasselt.dwi_application.controller.WorkInstruction.Manager.InstructionTreeItems;

import be.uhasselt.dwi_application.model.workInstruction.Instruction;

public class PickingTreeItemController extends instructionTreeItemController {

    public PickingTreeItemController() {
        super("/be/uhasselt/dwi_application/view/InstructionManager/instruction/PickingInstructionTreeCell.fxml");
    }
    
    @Override
    protected void updateItem(Instruction item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
            instructionLabel.setText("📦 " + item.getDescription());
            instructionLabel.setStyle("-fx-text-fill: green;");
        }
    }
}
