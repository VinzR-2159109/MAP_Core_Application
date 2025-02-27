package be.uhasselt.dwi_application.model.workInstruction;

import be.uhasselt.dwi_application.model.picking.Part;

public class PickingInstruction extends Instruction {
    private Part partToPick;
    private int quantity;

    public PickingInstruction(String description, Part partToPick, Long assemblyId, Long parentInstructionId, String imagePath, String hint, String properties, int quantity) {
        super(description, "PICKING", parentInstructionId, assemblyId, imagePath, hint, properties);
        this.partToPick = partToPick;
        this.quantity = quantity;
    }

    public PickingInstruction(String description, Part partToPick, Long assemblyId, Long parentInstructionId) {
        this(description, partToPick, assemblyId, parentInstructionId, null, null, null, 1);
    }

    public Part getPartToPick() { return partToPick; }
    public void setPartToPick(Part partToPick) { this.partToPick = partToPick; }
    public Long getPartId() {
        return partToPick != null ? partToPick.getId() : null;
    }

    public int getQuantity() {return quantity;}
    public void setQuantity(int quantity) {this.quantity = quantity;}

    @Override
    public String toString() {
        return super.toString() + " : " + partToPick;
    }
}
