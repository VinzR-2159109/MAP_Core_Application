package be.uhasselt.dwi_application.controller.WorkInstruction.Manager;

import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.model.workInstruction.AssemblyInstruction;
import be.uhasselt.dwi_application.model.workInstruction.Instruction;
import be.uhasselt.dwi_application.model.workInstruction.PickingInstruction;
import be.uhasselt.dwi_application.utility.database.repository.instruction.InstructionRepository;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import static be.uhasselt.dwi_application.utility.modules.Dialog.showErrorDialog;
import static be.uhasselt.dwi_application.utility.modules.Dialog.showInfoDialog;

public class InstructionManagerHelper {
    private final Assembly assembly;

    public InstructionManagerHelper(Assembly assembly) {
        this.assembly = assembly;
    }

    public void addAssemblyInstruction(TreeItem<Instruction> selectedNode) {
        if (selectedNode == null) {
            showErrorDialog("Error", "No instruction selected", "Please select an instruction before adding a sub-instruction.", "OK");
            return;
        }

        Instruction parentInstruction = selectedNode.getValue();
        if (!(parentInstruction instanceof AssemblyInstruction)) {
            showErrorDialog("Invalid Selection", "Cannot add a sub-instruction here", "You can only add sub-instructions to assembly instructions.", "OK");
            return;
        }

        AssemblyInstruction newInstruction = new AssemblyInstruction("New Assembly Instruction", assembly.getId(), parentInstruction.getId());
        InstructionRepository.getInstance().insertAssemblyInstruction(newInstruction);

        TreeItem<Instruction> subNode = new TreeItem<>(newInstruction);
        selectedNode.getChildren().add(subNode);
        selectedNode.setExpanded(true);
    }

    public void addPickingInstruction(TreeItem<Instruction> selectedNode) {
        if (selectedNode == null) {
            showErrorDialog("Error", "No instruction selected", "Please select an instruction before adding a picking instruction.", "OK");
            return;
        }


        Instruction parentInstruction = selectedNode.getValue();
        if (!(parentInstruction instanceof AssemblyInstruction)) {
            showErrorDialog("Invalid Selection", "Cannot add a sub-instruction here", "You can only add sub-instructions to assembly instructions.", "OK");
            return;
        }

        PickingInstruction newInstruction = new PickingInstruction("New Picking Instruction", null, assembly.getId(), parentInstruction.getId());
        InstructionRepository.getInstance().insertPickingInstruction(newInstruction);

        TreeItem<Instruction> subNode = new TreeItem<>(newInstruction);
        selectedNode.getChildren().add(subNode);
        selectedNode.setExpanded(true);
    }

    public void removeSelectedInstruction(TreeItem<Instruction> selectedNode, TreeView<Instruction> instructionTree) {
        if (selectedNode == null) {
            showInfoDialog("Warning", "No instruction selected", "Please select an instruction before removing.", "Close");
            return;
        }

        Instruction instruction = selectedNode.getValue();
        // Dont delete rootNode
        if (instruction.getParentInstructionId() != null) {
            InstructionRepository.getInstance().delete(instruction.getId());
            TreeItem<Instruction> parent = selectedNode.getParent();
            if (parent != null) {
                parent.getChildren().remove(selectedNode);
            } else {
                instructionTree.setRoot(null);
            }
        } else {
            showErrorDialog("Error", "Can't delete rootNode", "", "OK");
        }
    }
}
