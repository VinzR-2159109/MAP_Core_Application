package be.uhasselt.dwi_application.controller.WorkInstruction.Manager;

import be.uhasselt.dwi_application.model.picking.Part;
import be.uhasselt.dwi_application.model.workInstruction.Instruction;
import be.uhasselt.dwi_application.model.workInstruction.PickingInstruction;
import be.uhasselt.dwi_application.utility.database.repository.instruction.InstructionRepository;
import javafx.scene.control.TreeItem;

import java.util.Objects;

import static be.uhasselt.dwi_application.controller.WorkInstruction.Manager.InstructionLoader.loadInstructions;

public class DatabaseUpdater {
    private String originalHint;
    private String originalDescription;
    private Part originalPart;
    private int originalQuantity;

    private boolean isUpdatingUI;

    public DatabaseUpdater() {
        originalHint = "";
        originalDescription = "";
        originalQuantity = 0;

        originalPart = null;
        isUpdatingUI = false;
    }

    boolean updateHintInDB(TreeItem<Instruction> selectedNode, String hint) throws NullPointerException {
        if (selectedNode == null) throw new NullPointerException("Selected node is null");
        Instruction instruction = selectedNode.getValue();

        if (instruction == null) throw new NullPointerException("instruction is null");

        if (Objects.equals(originalHint, hint)) return false;

        instruction.setHint(hint);
        InstructionRepository.getInstance().updateInstruction(instruction);
        originalHint = hint;

        System.out.println("Updated hint in DB: " + hint);
        return true;
    }

    boolean updateDescriptionInDB(TreeItem<Instruction> selectedNode, String newDescription) {
        if (selectedNode == null) throw new NullPointerException("Selected node is null");
        Instruction instruction = selectedNode.getValue();

        if (instruction == null) throw new NullPointerException("instruction is null");

        if (Objects.equals(originalDescription, newDescription)) return false;

        instruction.setDescription(newDescription);
        InstructionRepository.getInstance().updateInstruction(instruction);
        originalDescription = newDescription;

        System.out.println("Updated description in DB: " + newDescription);
        return true;
    }

    boolean updatePartInDB(TreeItem<Instruction> selectedNode, Part selectedPart) {
        if (selectedNode == null) throw new NullPointerException("Selected node is null");
        Instruction instruction = selectedNode.getValue();

        if (instruction == null) throw new NullPointerException("instruction is null");
        if (!(instruction instanceof PickingInstruction pickingInstruction)) return false;

        if (Objects.equals(originalPart, selectedPart)  || isUpdatingUI) return false;

        pickingInstruction.setPartToPick(selectedPart);
        InstructionRepository.getInstance().updateInstruction(instruction);
        originalPart = selectedPart;

        System.out.println("Updated part in DB: " + selectedPart);
        return true;
    }

    boolean updateQuantityInDB(TreeItem<Instruction> selectedNode, int newQuantity) {
        if (selectedNode == null) throw new NullPointerException("Selected node is null");
        Instruction instruction = selectedNode.getValue();

        if (instruction == null) throw new NullPointerException("instruction is null");
        if (!(instruction instanceof PickingInstruction pickingInstruction)) return false;

        if (Objects.equals(originalQuantity, newQuantity)  || isUpdatingUI) return false;

        pickingInstruction.setQuantity(newQuantity);
        InstructionRepository.getInstance().updateInstruction(pickingInstruction);

        System.out.println("Updated quantity in DB: " + newQuantity);
        return true;
    }

    public void lockUpdates(){
        isUpdatingUI = true;
    }

    public void unlockUpdate(){
        isUpdatingUI = false;
    }

    public void setSelectedNode(TreeItem<Instruction> selectedNode) {
        Instruction instruction = selectedNode.getValue();

        originalHint = instruction.getHint();
        originalDescription = instruction.getDescription();

        if (instruction instanceof PickingInstruction pickingInstruction) {
            originalPart = pickingInstruction.getPartToPick();
        } else {
            originalPart = null;
        }
    }

}
