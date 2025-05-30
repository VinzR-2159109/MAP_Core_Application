package be.uhasselt.dwi_application.controller.AssemblyPlayer;

import be.uhasselt.dwi_application.controller.AssemblyPlayer.Assembly.AssemblyInstructionHandler;
import be.uhasselt.dwi_application.controller.AssemblyPlayer.Pick.PickInstructionHandler;
import be.uhasselt.dwi_application.controller.Controller;
import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.model.workInstruction.AssemblyInstruction;
import be.uhasselt.dwi_application.model.workInstruction.Instruction;
import be.uhasselt.dwi_application.model.workInstruction.picking.PickingInstruction;
import be.uhasselt.dwi_application.utility.database.repository.settings.Settings;
import be.uhasselt.dwi_application.utility.database.repository.settings.SettingsRepository;
import be.uhasselt.dwi_application.utility.exception.BinNotFoundException;
import be.uhasselt.dwi_application.utility.modules.SoundPlayer;
import be.uhasselt.dwi_application.utility.network.WebSocket.WebSocketRunner;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import static be.uhasselt.dwi_application.utility.modules.Dialog.showErrorDialogWithChoice;
import static be.uhasselt.dwi_application.utility.modules.Dialog.showExceptionDialog;
import static be.uhasselt.dwi_application.utility.modules.ImageHandler.loadImage;
import static be.uhasselt.dwi_application.utility.modules.SessionIDGenerator.generateSessionId;

public class AssemblyPlayerController implements Controller {
    @FXML private Button nok_btn;
    @FXML private Button ok_btn;
    @FXML private Label instructionDescription_lbl;
    @FXML private Label assemblyName_lbl;
    @FXML private ImageView InstructionImageView;
    @FXML private HBox hint_hbox;
    @FXML private Label hint_lbl;

    private Instruction currentInstruction;

    private final Assembly assembly;
    private final AssemblyPlayerManager manager;
    private final PickInstructionHandler pickInstructionHandler;
    private final AssemblyInstructionHandler assemblyInstructionHandler;
    private final Settings settings = SettingsRepository.loadSettings();

    private final WebSocketRunner websocket;

    private enum InstructionStatus {
        PENDING,
        COMPLETED_OK,
        COMPLETED_NOK
    }

    public AssemblyPlayerController(Assembly assembly) throws Exception {
        System.out.println("\u001B[32m" +  "<Creating Assembly Player>" + "\u001B[0m");
        this.assembly = assembly;

        String sessionId = generateSessionId();
        this.assemblyInstructionHandler = new AssemblyInstructionHandler(sessionId);
        this.pickInstructionHandler = new PickInstructionHandler(sessionId);

        this.manager = new AssemblyPlayerManager(assembly);
        this.currentInstruction = manager.moveToNextInstruction(null);

        this.websocket = new WebSocketRunner();
        websocket.connect();
    }

    @FXML
    private void initialize() {
        assemblyName_lbl.setText(assembly.getName());

        nok_btn.setOnAction(_ -> handleNok());
        ok_btn.setOnAction(_ -> handleOk());

        instructionDescription_lbl.setText(currentInstruction.getDescription());
        InstructionImageView.setImage(loadImage(currentInstruction.getImagePath()));

        hint_hbox.setVisible(false);

        setNextInstruction(currentInstruction);
    }

    public void setNextInstruction(Instruction instruction) {
        this.currentInstruction = instruction;
        System.out.println("\u001B[36m" + "/Next Instruction " + instruction + "\u001B[0m");

        if (instruction == null) {
            System.out.println("\u001B[31m" + "<All Instructions Completed>" + "\u001B[0m");

            instructionDescription_lbl.setText("All instructions completed!");
            InstructionImageView.setImage(null);

            ok_btn.setDisable(true);
            nok_btn.setDisable(true);

            currentInstruction = null;
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
        if (settings.isAssemblyAssistanceEnabled()){
            if (!pickInstructionHandler.isCompleted() && pickInstructionHandler.isRunning()){
                showErrorDialogWithChoice("Warning", "The system did not mark the picking as completed", "Are you sure that you completed the Picking Instruction correctly?", "Yes", "No",() -> {
                    pickInstructionHandler.stop();

                    hint_hbox.setVisible(false);
                    setNextInstruction(manager.moveToNextInstruction(currentInstruction));
                });
                return;
            }

            if (!assemblyInstructionHandler.isCompleted() && assemblyInstructionHandler.isRunning()){
                showErrorDialogWithChoice("Warning", "Het systeem heeft de assemblageInstructie niet as compleet gezien", "Ben je zeker dat de assemblage juist is uitgevoerd?", "Ja", "Nee",() -> {
                    assemblyInstructionHandler.stop();

                    hint_hbox.setVisible(false);
                    setNextInstruction(manager.moveToNextInstruction(currentInstruction));
                });
                return;
            }
        }
        hint_hbox.setVisible(false);
        setNextInstruction(manager.moveToNextInstruction(currentInstruction));
    }

    private void handleNok() {
        System.out.println("\u001B[31m" + "<handleNok>" + "\u001B[0m");

        if (!currentInstruction.getHint().isEmpty() && !currentInstruction.hasProperty(Instruction.InstructionProperty.HINT_DISABLED)){
            hint_hbox.setVisible(true);
            hint_lbl.setText(currentInstruction.getHint());
        }

        applyButtonStatus(InstructionStatus.COMPLETED_NOK);
    }

    private void handlePickingInstruction(PickingInstruction pickingInstruction) {
        System.out.println("\u001B[34m" + "<handlePickingInstruction>" + "\u001B[0m");
        try {
            pickInstructionHandler.start(pickingInstruction, () ->{
                applyButtonStatus(InstructionStatus.COMPLETED_OK);
            });
            applyButtonStatus(InstructionStatus.PENDING);
        } catch (BinNotFoundException e) {
            showExceptionDialog(e.getTitle(), e.getHeader(), e);
        }
    }

    private void handleAssemblyInstruction(AssemblyInstruction assemblyInstruction) {
        System.out.println("\u001B[33m" + "<handleAssemblyInstruction>" + "\u001B[0m");
        try {
            assemblyInstructionHandler.start(assemblyInstruction, () -> {
                SoundPlayer.play(SoundPlayer.SoundType.OK);
                applyButtonStatus(InstructionStatus.COMPLETED_OK);
            });
            applyButtonStatus(InstructionStatus.PENDING);
        } catch (Exception e) {
            showExceptionDialog("Error", "handleAssemblyInstruction", e);
        }
    }

    private void applyButtonStatus(InstructionStatus status) {
        switch (status) {
            case PENDING:
                ok_btn.setStyle("-fx-background-color: rgb(255,149,0); -fx-text-fill: white; -fx-font-size: 72px");
                nok_btn.setStyle("-fx-background-color: #cc0000; -fx-text-fill: white; -fx-font-size: 72px");
                break;
            case COMPLETED_OK:
                ok_btn.setStyle("-fx-background-color: #00ff00; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 72px");
                nok_btn.setStyle("-fx-background-color: #cc0000; -fx-text-fill: white; -fx-font-size: 72px");
                break;
            case COMPLETED_NOK:
                nok_btn.setStyle("-fx-background-color: #cc0000; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 72px");
                break;
        }
    }

    public void cleanup() {
        System.out.println("AssemblyPlayerController is being destroyed!");

        if (pickInstructionHandler != null) {
            pickInstructionHandler.stop();
        }

        if (assemblyInstructionHandler != null) {
            assemblyInstructionHandler.stop();
        }
    }
}
