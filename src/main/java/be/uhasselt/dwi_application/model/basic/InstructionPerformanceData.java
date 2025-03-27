package be.uhasselt.dwi_application.model.basic;

import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.model.workInstruction.Instruction;

public record InstructionPerformanceData(String id, Assembly assembly, Time executionTime, Instruction instruction) {

    @Override
    public String toString() {
        return "id= " + id +
                ",assembly= " + assembly +
                ", executionTime= " + executionTime.getReadable() +
                ", instruction= " + instruction;
    }
}
