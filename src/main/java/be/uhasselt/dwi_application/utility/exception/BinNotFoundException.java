package be.uhasselt.dwi_application.utility.exception;

import be.uhasselt.dwi_application.model.workInstruction.picking.Part;

public class BinNotFoundException extends Exception {
    private final Part part;

    public BinNotFoundException(Part part) {
        super("Check if part is assigned to a bin in InstructionManager or BinManager");
        this.part = part;
    }

    public String getTitle() {
        return "Error";
    }

    public String getHeader() {
        return "PickingBin not found for " + part.getName() + " (Id: " + part.getId() + ")";
    }

    public String getPartName() {
        return part.getName();
    }

    public Long getPartId() {
        return part.getId();
    }
}
