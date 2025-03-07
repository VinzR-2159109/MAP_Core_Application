package be.uhasselt.dwi_application.model.workInstruction;

import be.uhasselt.dwi_application.model.basic.Position;
import be.uhasselt.dwi_application.utility.database.repository.instruction.InstructionRepository;
import be.uhasselt.dwi_application.utility.database.repository.position.PositionRepository;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class AssemblyInstruction extends Instruction {
    private List<Instruction> subInstructions = new ArrayList<>();
    private List<Position> assemblyPositions;

    public AssemblyInstruction(String description, Long assemblyId, Long parentInstructionId, String imagePath, String hint, String properties, List<Position> assemblyPositions) {
        super(description, "ASSEMBLY", parentInstructionId, assemblyId, imagePath, hint, properties);
        this.assemblyPositions = assemblyPositions;
    }

    public AssemblyInstruction(String description, Long assemblyId, Long parentId) {
        this(description, assemblyId, parentId, null, null, null, null);
    }

    public List<Instruction> getSubInstructions() {
        return subInstructions;
    }
    public void addSubInstruction(Instruction subInstruction) {
        subInstruction.setParentInstructionId(this.getId());
        subInstructions.add(subInstruction);
    }

    public List<Position> getAssemblyPositions() {
        if (assemblyPositions.isEmpty()) {
            assemblyPositions = PositionRepository.getInstance().getAllByInstructionId(this.getId());
        }
        return assemblyPositions;
    }
    public void setAssemblyPositions(List<Position> assemblyPositions) {this.assemblyPositions = assemblyPositions;}

    public void addPosition(Position position) {assemblyPositions.add(position);    }
    public void addPositions(List<Position> newPositions) {assemblyPositions.addAll(newPositions);}
    public void clearPositions() { assemblyPositions.clear();}

}
