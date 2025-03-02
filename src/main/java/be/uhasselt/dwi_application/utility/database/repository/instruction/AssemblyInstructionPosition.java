package be.uhasselt.dwi_application.utility.database.repository.instruction;

public class AssemblyInstructionPosition {
    private Long instructionId;
    private Long positionId;
    private double x;
    private double y;

    public AssemblyInstructionPosition(Long instructionId, Long positionId, double x, double y) {
        this.instructionId = instructionId;
        this.positionId = positionId;
        this.x = x;
        this.y = y;
    }

    public Long getInstructionId() { return instructionId; }
    public Long getPositionId() { return positionId; }
    public double getX() { return x; }
    public double getY() { return y; }
}

