package be.uhasselt.dwi_application.controller.WorkInstruction.Manager;

import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.model.workInstruction.AssemblyInstruction;
import be.uhasselt.dwi_application.model.workInstruction.Instruction;
import be.uhasselt.dwi_application.utility.database.repository.instruction.InstructionRepository;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static be.uhasselt.dwi_application.utility.modules.Dialog.showErrorDialog;

public class InstructionLoader {
    public static void loadInstructions(Assembly assembly, TreeItem<Instruction> selectedNode, TreeView<Instruction> instructionTree) {
        if (assembly == null) {
            showErrorDialog("Error", "No assembly selected", "Please select an assembly before loading instructions.", "OK");
            return;
        }

        // Store the ID of the selected instruction
        Long selectedInstructionId = (selectedNode != null) ? selectedNode.getValue().getId() : null;

        ArrayList<Instruction> instructions = (ArrayList<Instruction>) InstructionRepository.getInstance().findByAssembly(assembly);
        Map<Long, TreeItem<Instruction>> itemMap = new HashMap<>();
        TreeItem<Instruction> root = null;
        TreeItem<Instruction> nodeToSelect = null;

        for (Instruction instruction : instructions) {
            TreeItem<Instruction> item = new TreeItem<>(instruction);
            itemMap.put(instruction.getId(), item);

            if (instruction.getParentInstructionId() == null) {
                if (root != null) {
                    showErrorDialog("Error", "Multiple root instructions found", "Please check database integrity.", "OK");
                    return;
                }
                root = item;
            }

            // Check if this is the previously selected instruction
            if (instruction.getId().equals(selectedInstructionId)) {
                nodeToSelect = item;
            }
        }

        if (root == null) {
            AssemblyInstruction rootInstruction = new AssemblyInstruction(assembly.getName(), assembly.getId(), null);
            InstructionRepository.getInstance().insertAssemblyInstruction(rootInstruction);
            root = new TreeItem<>(rootInstruction);
            itemMap.put(rootInstruction.getId(), root);
            instructions.add(rootInstruction);
        }

        for (Instruction instruction : instructions) {
            if (instruction.getParentInstructionId() != null) {
                TreeItem<Instruction> parent = itemMap.get(instruction.getParentInstructionId());
                if (parent != null) {
                    parent.getChildren().add(itemMap.get(instruction.getId()));
                    parent.setExpanded(true);
                }
            }
        }

        instructionTree.setRoot(root);
        instructionTree.setShowRoot(true);

        // Restore selection if possible
        if (nodeToSelect != null) {
            instructionTree.getSelectionModel().select(nodeToSelect);
            selectedNode = nodeToSelect;
        }
    }
}
