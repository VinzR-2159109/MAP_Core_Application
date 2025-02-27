package be.uhasselt.dwi_application.model.workInstruction;

import java.util.ArrayList;
import java.util.List;

public class AssemblyInstruction extends Instruction {
    private List<Instruction> subInstructions = new ArrayList<>();

    public AssemblyInstruction(String description, Long assemblyId, Long parentInstructionId, String imagePath, String hint, String properties) {
        super(description, "ASSEMBLY", parentInstructionId, assemblyId, imagePath, hint, properties);
    }

    public AssemblyInstruction(String description, Long assemblyId, Long parentId) {
        this(description, assemblyId, parentId, null, null, null);
    }

    public List<Instruction> getSubInstructions() {
        return subInstructions;
    }

    public void addSubInstruction(Instruction subInstruction) {
        subInstruction.setParentInstructionId(this.getId());
        subInstructions.add(subInstruction);
    }
}
