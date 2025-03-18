package be.uhasselt.dwi_application.controller.WorkInstruction.Manager;

import be.uhasselt.dwi_application.controller.Controller;
import be.uhasselt.dwi_application.controller.MainController;
import be.uhasselt.dwi_application.controller.WorkInstruction.LocationPicker.AssemblyLocationPickerController;
import be.uhasselt.dwi_application.controller.WorkInstruction.Part.PartManagerController;
import be.uhasselt.dwi_application.model.workInstruction.*;
import be.uhasselt.dwi_application.model.picking.Part;
import be.uhasselt.dwi_application.utility.FxmlViews;
import be.uhasselt.dwi_application.utility.database.repository.instruction.InstructionRepository;
import be.uhasselt.dwi_application.utility.modules.ImageHandler;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.io.IOException;
import java.util.*;

import static be.uhasselt.dwi_application.controller.WorkInstruction.Manager.InstructionLoader.loadInstructions;
import static be.uhasselt.dwi_application.utility.modules.Dialog.*;
import static be.uhasselt.dwi_application.utility.modules.ImageHandler.uploadImage;

public class InstructionManagerController implements Controller {
    @FXML private Button removeInstruction_btn;
    @FXML private TreeView<Instruction> instructionTree;
    @FXML private HBox pickInstructionSettings_hbox;
    @FXML private ComboBox<Part> partSelector;
    @FXML private AnchorPane partManagerContainer;
    @FXML private ImageView InstructionImageView;
    @FXML private Button uploadImage_btn;
    @FXML private Button deleteImage_btn;
    @FXML private CheckBox disableHint_checkbox;
    @FXML private Label hintLabel;
    @FXML private TextField hintField;
    @FXML private CheckBox skipDuringPlay_checkbox;
    @FXML private Spinner<Integer> partQuantiy_spinner;
    @FXML private Button pickLocation_btn;
    @FXML private HBox location_hbox;

    private TreeItem<Instruction> selectedNode;
    private final Assembly assembly;
    private static InstructionManagerController instance;
    private InstructionManagerHelper helper;
    private DatabaseUpdater updater;

    public InstructionManagerController(Assembly assembly) {
        helper = new InstructionManagerHelper(assembly);
        updater = new DatabaseUpdater();
        this.assembly = assembly;
    }

    public static InstructionManagerController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        System.out.println("---Initializing Instruction Manager---");
        instructionTree.setOnMouseClicked(this::handleTreeSelection);
        instructionTree.setCellFactory(_ -> new InstructionTreeItemController(assembly));


        instance = this;

        removeInstruction_btn.setOnAction(_ -> {
            helper.removeSelectedInstruction(selectedNode, instructionTree);
            selectedNode = null;
        });

        uploadImage_btn.setOnAction(_ -> uploadInstructionImage());
        deleteImage_btn.setOnAction(_ -> deleteImage());
        disableHint_checkbox.setOnAction(_ -> toggleDisableHint());
        skipDuringPlay_checkbox.setOnAction(_ -> toggleSkipDuringPlay());
        pickLocation_btn.setOnAction(_-> openLocationPicker());
        pickInstructionSettings_hbox.setDisable(true);
        partQuantiy_spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FxmlViews.PART_MANAGER));
            loader.setControllerFactory(_-> new PartManagerController(assembly));
            VBox partContainer = loader.load();
            partManagerContainer.getChildren().setAll(partContainer);
        } catch (IOException e) {
            showErrorDialog("Error", "Failed to load Part Manager", e.getMessage(), "OK");
        }

        setupTypingTimer(hintField, this::updateHint);

        loadInstructions(assembly, selectedNode, instructionTree);
        populatePartSelector();
        partSelector.valueProperty().addListener((_, _, selectedPart) -> updatePartInDB(selectedPart));
        partQuantiy_spinner.valueProperty().addListener((_, _, newValue) -> updateQuantityInDB(newValue));

    }

    private void openLocationPicker() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FxmlViews.LOCATION_PICKER));
            if (selectedNode != null) {
                Instruction instruction = selectedNode.getValue();
                if (instruction instanceof AssemblyInstruction assemblyInstruction) {
                    loader.setControllerFactory(_-> new AssemblyLocationPickerController(assembly, assemblyInstruction));
                    Parent instructionView = loader.load();
                    Controller controller = loader.getController();

                    MainController.getInstance().setContentView(instructionView, controller);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showExceptionDialog("Fault in handleTileClick", e);
        }
    }

    public void populatePartSelector() {
        partSelector.getItems().clear();
        List<Part> parts = assembly.getAllParts();
        partSelector.getItems().addAll(parts);

        System.out.println("Loading parts in partSelector: " + parts);
    }

    private void handleTreeSelection(MouseEvent event) {
        selectedNode = instructionTree.getSelectionModel().getSelectedItem();
        if (selectedNode != null) {
            Instruction instruction = selectedNode.getValue();
            updater.lockUpdates();
            updater.setSelectedNode(selectedNode);

            hintField.setText(instruction.getHint() != null ? instruction.getHint() : "");
            disableHint_checkbox.setSelected(instruction.hasProperty(Instruction.InstructionProperty.HINT_DISABLED));
            skipDuringPlay_checkbox.setSelected(instruction.hasProperty(Instruction.InstructionProperty.SKIP_DURING_PLAY));

            location_hbox.setDisable(instruction.hasProperty(Instruction.InstructionProperty.SKIP_DURING_PLAY));

            // Handle Part Selection
            if (instruction instanceof PickingInstruction pickingInstruction) {
                pickInstructionSettings_hbox.setDisable(false);
                partSelector.setValue(pickingInstruction.getPartToPick());
                partQuantiy_spinner.getValueFactory().setValue(pickingInstruction.getQuantity());
                location_hbox.setDisable(true);
            } else if (instruction instanceof AssemblyInstruction assemblyInstruction) {
                location_hbox.setDisable(false);
                pickInstructionSettings_hbox.setDisable(true);
                partSelector.setValue(null);
            }

            // Load Image
            String imagePath = InstructionRepository.getInstance().getInstructionImagePath(instruction.getId());
            InstructionImageView.setImage(imagePath != null ? ImageHandler.loadImage(imagePath) : null);

            updater.unlockUpdate();
        }
    }


    private void toggleDisableHint() {
        boolean isDisabled = disableHint_checkbox.isSelected();
        if (selectedNode == null) return;
        if (isDisabled) {
            hintField.setDisable(true);
            hintLabel.setDisable(true);

            Instruction selectedInstruction = selectedNode.getValue();
            selectedInstruction.setHint(null);
            selectedInstruction.addProperty(Instruction.InstructionProperty.HINT_DISABLED);
            InstructionRepository.getInstance().updateInstruction(selectedNode.getValue());

            System.out.println("Disabled Hints for: " + selectedInstruction);
        } else {
            hintField.setDisable(false);
            hintLabel.setDisable(false);

            Instruction selectedInstruction = selectedNode.getValue();
            selectedInstruction.setHint(hintField.getText());
            selectedInstruction.removeProperty(Instruction.InstructionProperty.HINT_DISABLED);
            InstructionRepository.getInstance().updateInstruction(selectedNode.getValue());

            System.out.println("Enabled Hints for: " + selectedInstruction);
        }
    }

    private void toggleSkipDuringPlay() {
        boolean isSkipped = skipDuringPlay_checkbox.isSelected();
        if (selectedNode == null) return;
        if (isSkipped) {
            Instruction selectedInstruction = selectedNode.getValue();
            selectedInstruction.addProperty(Instruction.InstructionProperty.SKIP_DURING_PLAY);
            InstructionRepository.getInstance().updateInstruction(selectedNode.getValue());

            System.out.println("Enabled skip during play: " + selectedInstruction);
        } else {
            hintField.setDisable(false);

            Instruction selectedInstruction = selectedNode.getValue();
            selectedInstruction.removeProperty(Instruction.InstructionProperty.SKIP_DURING_PLAY);
            InstructionRepository.getInstance().updateInstruction(selectedNode.getValue());

            System.out.println("Disabled skip during play: " + selectedInstruction);
        }
    }

    private void setupTypingTimer(TextField field, Runnable updateMethod) {
        Timeline typingTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateMethod.run()));
        typingTimer.setCycleCount(1);

        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (selectedNode != null && oldValue != null && !oldValue.equals(newValue)) {
                typingTimer.stop();
                typingTimer.playFromStart();
            }
        });
    }

    private void updateHint() {
       boolean success = updater.updateHintInDB(selectedNode, hintField.getText());
       if (success) reloadUI();
    }

    private void updatePartInDB(Part selectedPart) {
        boolean success = updater.updatePartInDB(selectedNode, selectedPart);
        if (success) reloadUI();
    }

    private void updateQuantityInDB(int newQuantity) {
        updater.updateQuantityInDB(selectedNode, newQuantity);
    }

    private void uploadInstructionImage() {
        try {
            Image image = uploadImage(InstructionImageView.getScene().getWindow());
            InstructionRepository.getInstance().uploadImage(image.getUrl(), selectedNode.getValue().getId());
        } catch (IOException e) {
            showExceptionDialog("Error while uploading Image", e);
        }
    }
    private void deleteImage() {InstructionImageView.setImage(null);}

    private void reloadUI(){
        loadInstructions(assembly, selectedNode, instructionTree);
    }

    @Override
    public void cleanup() {

    }
}
