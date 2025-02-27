package be.uhasselt.dwi_application.controller.AssemblyPlayer;

import be.uhasselt.dwi_application.controller.Controller;
import be.uhasselt.dwi_application.model.workInstruction.Assembly;
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
    private final PickInstructionHandler pickInstructionHandler;

    public AssemblyPlayerController(Assembly assembly) {
        this.assembly = assembly;
        pickInstructionHandler = new PickInstructionHandler(PickInstructionHandler.PickingHand.RIGHT);
        manager = new AssemblyPlayerManager(assembly);

        this.currentInstruction = manager.moveToNextInstruction(null);
        if (currentInstruction instanceof PickingInstruction pickingInstruction) {
            handlePickingInstruction(pickingInstruction);
        }
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
        } else {
            updateUI();
        }
    }


    private void handleOk() {
        if (pickInstructionHandler.isRunning()){
            showErrorDialogWithChoice("Warning", "The system did not mark the picking as completed", "Are you sure that you completed the Picking Instruction correctly?", "Yes", "No",() -> {
                pickInstructionHandler.stop();
                setNextInstruction(manager.moveToNextInstruction(currentInstruction));
            });
            return;
        }

        //Play Ok-sound, Green lights ...
        setNextInstruction(manager.moveToNextInstruction(currentInstruction));
    }

    private void updateUI() {
        Platform.runLater(() -> {
            instructionDescription_lbl.setText(currentInstruction.getDescription());
            InstructionImageView.setImage(loadImage(currentInstruction.getImagePath()));
        });

        if (currentInstruction instanceof PickingInstruction pickingInstruction) {
            handlePickingInstruction(pickingInstruction);
        }
    }

    private void handleNok() {
        System.out.println("Nok");
        //Play Nok-sound, Red lights ...
    }

    private void handlePickingInstruction(PickingInstruction pickingInstruction) {
        try {
            pickInstructionHandler.start(pickingInstruction, () -> {
                System.out.println("<Pick Completion detected automatically>");
                handleOk();
            });
        } catch (BinNotFoundException e) {
            showExceptionDialog(e.getTitle(), e.getHeader(), e);
        }
    }

    public void cleanup() {
        System.out.println("AssemblyPlayerController is being destroyed!");
        // Stop any running background tasks
        if (pickInstructionHandler != null && pickInstructionHandler.isRunning()) {
            pickInstructionHandler.stop();
        }
    }


}
