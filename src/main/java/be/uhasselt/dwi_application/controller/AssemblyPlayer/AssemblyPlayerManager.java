package be.uhasselt.dwi_application.controller.AssemblyPlayer;

import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.model.workInstruction.AssemblyInstruction;
import be.uhasselt.dwi_application.model.workInstruction.Instruction;
import be.uhasselt.dwi_application.utility.database.repository.instruction.InstructionRepository;

import java.util.List;

public class AssemblyPlayerManager {
    private final List<Instruction> instructions;

    public AssemblyPlayerManager(Assembly assembly) {
        this.instructions = InstructionRepository.getInstance().findByAssembly(assembly);
    }

    public Instruction moveToNextInstruction(Instruction previousInstruction) {
        System.out.println("\u001B[33m" + "<Moving to Next Instruction>" + "\u001B[0m");

        if (previousInstruction == null) {
            System.out.println("!! Finding First !!");
            return findFirstInstruction();
        }

        Instruction next = getNextPlayableInstruction(getNextInstruction(previousInstruction));
        if (next != null) {
            return next;
        } else {
            System.out.println("\u001B[31m" + "<No Next Instruction Found>" + "\u001B[0m");
            return null;
        }
    }

    private Instruction findFirstInstruction() {
        for (Instruction instruction : instructions) {
            if (instruction.getParentInstructionId() == null) {
                return getNextPlayableInstruction(instruction);
            }
        }
        return null;
    }

    private Instruction getNextPlayableInstruction(Instruction instruction) {
        while (instruction != null && instruction.hasProperty(Instruction.InstructionProperty.SKIP_DURING_PLAY)) {
            instruction = getNextInstruction(instruction);
        }
        return instruction;
    }

    private Instruction getNextInstruction(Instruction instruction) {
        System.out.println("<Getting Next Instruction>");
        // if it's an AssemblyInstruction with sub-instructions, go deeper
        if (instruction instanceof AssemblyInstruction) {
            List<Instruction> subInstructions = ((AssemblyInstruction) instruction).getSubInstructions();
            if (!subInstructions.isEmpty()) return subInstructions.getFirst();
        }

        // if no sub-instructions, check for a sibling instruction
        List<Instruction> siblings = findSiblings(instruction);
        int index = siblings.indexOf(instruction);
        if (index != -1 && index + 1 < siblings.size()) return siblings.get(index + 1);

        // if no sibling, move up the hierarchy to find the next instruction
        Instruction parent = findParent(instruction, instructions);
        while (parent != null) {
            List<Instruction> parentSiblings = findSiblings(parent);
            index = parentSiblings.indexOf(parent);
            if (index != -1 && index + 1 < parentSiblings.size()) {
                return parentSiblings.get(index + 1);
            }
            parent = findParent(parent, instructions); // Move up further
        }
        return null;
    }

    private Instruction findParent(Instruction target, List<Instruction> instructions) {
        for (Instruction instr : instructions) {
            if (instr instanceof AssemblyInstruction) {
                List<Instruction> subInstructions = ((AssemblyInstruction) instr).getSubInstructions();
                if (subInstructions.contains(target)) return instr; // Found parent
                Instruction found = findParent(target, subInstructions);
                if (found != null) return found;
            }
        }
        return null;
    }

    private List<Instruction> findSiblings(Instruction instruction) {
        Instruction parent = findParent(instruction, instructions);
        if (parent instanceof AssemblyInstruction) {
            return ((AssemblyInstruction) parent).getSubInstructions();
        }
        return instructions;
    }
}
