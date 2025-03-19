package be.uhasselt.dwi_application.controller.WorkInstruction.Manager.InstructionTreeItems;

import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.model.workInstruction.AssemblyInstruction;
import be.uhasselt.dwi_application.model.workInstruction.Instruction;
import be.uhasselt.dwi_application.model.workInstruction.PickingInstruction;
import be.uhasselt.dwi_application.utility.database.repository.instruction.InstructionRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TreeItem;
import javafx.collections.FXCollections;

public class AssemblyTreeItemController extends instructionTreeItemController {
    @FXML private Button addSubInstruction_btn;
    @FXML private ComboBox<String> addInstructionType_combo;

    private final Assembly assembly;

    public AssemblyTreeItemController(Assembly assembly) {
        super("/be/uhasselt/dwi_application/view/InstructionManager/instruction/AssemblyInstructionTreeCell.fxml");
        this.assembly = assembly;

        addInstructionType_combo.setItems(FXCollections.observableArrayList("Assembly Instruction", "Picking Instruction"));
        addInstructionType_combo.setVisible(false);
        addInstructionType_combo.setManaged(false);

        addSubInstruction_btn.setOnAction(e -> {
            addInstructionType_combo.setVisible(true);
            addInstructionType_combo.setManaged(true);
            addInstructionType_combo.show();
        });

        addInstructionType_combo.setOnAction(e -> {
            String selectedType = addInstructionType_combo.getValue();
            if (selectedType != null) {
                addSubInstruction(getItem(), selectedType);
                addInstructionType_combo.setVisible(false);
                addInstructionType_combo.setManaged(false);
            }
        });
    }

    private void addSubInstruction(Instruction item, String type) {
        if (!(item instanceof AssemblyInstruction)) {
            return;
        }

        Instruction newInstruction = type.equals("Assembly Instruction") ?
                new AssemblyInstruction("New Assembly Instruction", assembly.getId(), item.getId()) :
                new PickingInstruction("New Picking Instruction", null, assembly.getId(), item.getId());

        if (newInstruction instanceof AssemblyInstruction) {
            InstructionRepository.getInstance().insertAssemblyInstruction((AssemblyInstruction) newInstruction);
        } else {
            InstructionRepository.getInstance().insertPickingInstruction((PickingInstruction) newInstruction);
        }

        TreeItem<Instruction> subNode = new TreeItem<>(newInstruction);
        getTreeItem().getChildren().add(subNode);
        getTreeItem().setExpanded(true);
        getTreeView().refresh();
    }
}
