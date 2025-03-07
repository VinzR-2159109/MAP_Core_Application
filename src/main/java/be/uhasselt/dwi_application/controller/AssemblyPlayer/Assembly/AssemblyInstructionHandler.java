package be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly;

import be.uhasselt.dwi_application.model.basic.Position;
import be.uhasselt.dwi_application.model.workInstruction.AssemblyInstruction;
import be.uhasselt.dwi_application.utility.database.repository.settings.SettingsRepository;

import java.util.List;

public class AssemblyInstructionHandler {
    private AssemblyInstruction assemblyInstruction;
    private AssemblyMQTTHelper helper;

    public AssemblyInstructionHandler(){
        helper = new AssemblyMQTTHelper();
    }

    public void start(AssemblyInstruction assemblyInstruction, Runnable onCompleteCallback){
        this.assemblyInstruction = assemblyInstruction;

        List<Position> positions = assemblyInstruction.getAssemblyPositions();

        if (positions.isEmpty()) {
            System.out.println("No assembly positions available.");
            return;
        }

        // Determine startX, endX for LED Strip X
        int startX = (int) positions.stream().mapToDouble(Position::getX).min().orElse(0);
        int endX = (int) positions.stream().mapToDouble(Position::getX).max().orElse(0);

        // Determine startY, endY for LED Strip Y
        int startY = (int) positions.stream().mapToDouble(Position::getY).min().orElse(0);
        int endY = (int) positions.stream().mapToDouble(Position::getY).max().orElse(0);


        int gridSize = SettingsRepository.loadSettings().getGridSize();

        // Send MQTT Commands
        helper.sendSetLedStripXGreenOnRange(startX/gridSize, endX/gridSize);
       // helper.sendSetLedStripYGreenOnRange(startY/gridSize, endY/gridSize);
    }

    public void stop(){
        helper.sendTurnOffAllLedStrip();
    }
}
