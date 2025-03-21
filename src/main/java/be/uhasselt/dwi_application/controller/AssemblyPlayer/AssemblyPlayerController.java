package be.uhasselt.dwi_application.controller.AssemblyPlayer;

import be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyInstructionHandler;
import be.uhasselt.dwi_application.controller.AssemblyPlayer.Pick.PickInstructionHandler;
import be.uhasselt.dwi_application.controller.Controller;
import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.model.workInstruction.AssemblyInstruction;
import be.uhasselt.dwi_application.model.workInstruction.Instruction;
import be.uhasselt.dwi_application.model.workInstruction.PickingInstruction;
import be.uhasselt.dwi_application.utility.exception.BinNotFoundException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import static be.uhasselt.dwi_application.utility.modules.Dialog.showErrorDialogWithChoice;
import static be.uhasselt.dwi_application.utility.modules.Dialog.showExceptionDialog;
import static be.uhasselt.dwi_application.utility.modules.ImageHandler.loadImage;

public class AssemblyPlayerController implements Controller {
    @FXML private Button nok_btn;
    @FXML private Button ok_btn;
    @FXML private Label instructionDescription_lbl;
    @FXML private Label assemblyName_lbl;
    @FXML private ImageView InstructionImageView;

    private final AssemblyPlayerManager manager;
    private Instruction currentInstruction;
    private final Assembly assembly;
    private PickInstructionHandler pickInstructionHandler;
    private AssemblyInstructionHandler assemblyInstructionHandler;

    public AssemblyPlayerController(Assembly assembly) {
        this.assembly = assembly;

        manager = new AssemblyPlayerManager(assembly);

        this.currentInstruction = manager.moveToNextInstruction(null);
        setNextInstruction(currentInstruction);
    }

    @FXML
    private void initialize() {
        nok_btn.setOnAction(_ -> handleNok());
        ok_btn.setOnAction(_ -> handleOk());
        assemblyName_lbl.setText(assembly.getName());

        instructionDescription_lbl.setText(currentInstruction.getDescription());
        InstructionImageView.setImage(loadImage(currentInstruction.getImagePath()));
    }

    public void setNextInstruction(Instruction instruction) {
        this.currentInstruction = instruction;
        if (instruction == null) {
            instructionDescription_lbl.setText("All instructions completed!");
            InstructionImageView.setImage(null);
            ok_btn.setDisable(true);
            nok_btn.setDisable(true);
            return;
        }

        if (currentInstruction instanceof PickingInstruction pickingInstruction) {
            handlePickingInstruction(pickingInstruction);
        }

        if (currentInstruction instanceof AssemblyInstruction assemblyInstruction) {
            handleAssemblyInstruction(assemblyInstruction);
        }

        Platform.runLater(() -> {
            instructionDescription_lbl.setText(currentInstruction.getDescription());
            InstructionImageView.setImage(loadImage(currentInstruction.getImagePath()));
        });
    }

    private void handleOk() {
        if (!pickInstructionHandler.isCompleted()){
            showErrorDialogWithChoice("Warning", "The system did not mark the picking as completed", "Are you sure that you completed the Picking Instruction correctly?", "Yes", "No",() -> {
                pickInstructionHandler.stop();
                setNextInstruction(manager.moveToNextInstruction(currentInstruction));
            });
            return;
        }

        if (!assemblyInstructionHandler.isCompleted()){
            showErrorDialogWithChoice("Warning", "Het systeem heeft de assemblageInstructie niet as compleet gezien", "Ben je zeker dat de assemblage juist is uitgevoerd?", "Ja", "Nee",() -> {
                assemblyInstructionHandler.stop();
                setNextInstruction(manager.moveToNextInstruction(currentInstruction));
            });
            return;
        }

        //Play Ok-sound, Green lights ...
        setNextInstruction(manager.moveToNextInstruction(currentInstruction));
    }

    private void handleNok() {
        System.out.println("Nok");
        //Play Nok-sound, Red lights ...
    }

    private void handlePickingInstruction(PickingInstruction pickingInstruction) {
        this.pickInstructionHandler = new PickInstructionHandler();
        try {
            pickInstructionHandler.start(pickingInstruction, () ->{
                setNextInstruction(manager.moveToNextInstruction(currentInstruction));
            });
        } catch (BinNotFoundException e) {
            showExceptionDialog(e.getTitle(), e.getHeader(), e);
        }
    }

    private void handleAssemblyInstruction(AssemblyInstruction assemblyInstruction) {
        this.assemblyInstructionHandler = new AssemblyInstructionHandler();
        try {
            assemblyInstructionHandler.start(assemblyInstruction, () -> {
                setNextInstruction(manager.moveToNextInstruction(currentInstruction));
            });
        } catch (Exception e) {
            showExceptionDialog("Error", "handleAssemblyInstruction", e);
        }
    }


    public void cleanup() {
        System.out.println("AssemblyPlayerController is being destroyed!");
        // Stop any running background tasks
        if (pickInstructionHandler != null) {
            pickInstructionHandler.stop();
        }

        if (assemblyInstructionHandler != null) {
            assemblyInstructionHandler.stop();
        }
    }
}
