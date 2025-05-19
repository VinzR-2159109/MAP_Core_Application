package be.uhasselt.dwi_application.controller.AssemblyPlayer;

import be.uhasselt.dwi_application.model.basic.InstructionPerformanceData;
import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.model.workInstruction.Instruction;
import be.uhasselt.dwi_application.utility.excel.ExcelWriter;
import be.uhasselt.dwi_application.utility.modules.ConsoleColors;
import be.uhasselt.dwi_application.utility.modules.ElapsedTimer;

public class InstructionMeasurementHandler {
    private final ElapsedTimer elapsedTimer;
    private final Assembly assembly;
    private final Instruction instruction;
    private final String id;

    public InstructionMeasurementHandler(Assembly assembly, Instruction instruction, String id) {
        this.elapsedTimer = new ElapsedTimer();
        this.assembly = assembly;
        this.instruction = instruction;
        this.id = id;
    }

    public void startMeasurement(){
        elapsedTimer.start();
    }

    public void stopMeasurement(){
        System.out.println(ConsoleColors.RED + "<Stopping Measurement>" + ConsoleColors.RESET);

        elapsedTimer.stop();

        InstructionPerformanceData performanceData = new InstructionPerformanceData(id, assembly, elapsedTimer.getElapsedTime(), instruction);
        ExcelWriter.append(performanceData);
    }

}
