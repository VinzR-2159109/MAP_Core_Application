package be.uhasselt.dwi_application.controller.WorkInstruction;

import be.uhasselt.dwi_application.controller.Controller;
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

import static be.uhasselt.dwi_application.utility.modules.Dialog.*;
import static be.uhasselt.dwi_application.utility.modules.ImageHandler.uploadImage;

public class InstructionManagerController implements Controller {
    @FXML private Button addAssemblyInstruction_btn;
    @FXML private Button addPickingInstruction_btn;
    @FXML private Button removeInstruction_btn;
    @FXML private TreeView<Instruction> instructionTree;
    @FXML private TextField descriptionField;
    @FXML private HBox pickInstructionSettings_hbox;
    @FXML private ComboBox<Part> partSelector;
    @FXML private AnchorPane partManagerContainer;
    @FXML private ImageView InstructionImageView;
    @FXML private Button uploadImage_btn;
    @FXML private Button deleteImage_btn;
    @FXML private CheckBox disableHint_checkbox;
    @FXML private TextField hintField;
    @FXML private CheckBox skipDuringPlay_checkbox;
    @FXML private Spinner<Integer> partQuantiy_spinner;

    private TreeItem<Instruction> selectedNode;
    private final Assembly assembly;
    private static InstructionManagerController instance;

    private String originalDescription = "";
    private String originalHint = "";
    private Part originalPart;

    private boolean isUpdatingUI = false;

    public InstructionManagerController(Assembly assembly) {
        this.assembly = assembly;
    }

    public static InstructionManagerController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        System.out.println("---Initializing Instruction Manager---");
        instructionTree.setOnMouseClicked(this::handleTreeSelection);
        instance = this;

        addAssemblyInstruction_btn.setOnAction(_ -> addAssemblyInstruction());
        addPickingInstruction_btn.setOnAction(_ -> addPickingInstruction());
        removeInstruction_btn.setOnAction(_ -> removeSelectedInstruction());
        uploadImage_btn.setOnAction(_ -> uploadInstructionImage());
        deleteImage_btn.setOnAction(_ -> deleteImage());
        disableHint_checkbox.setOnAction(_ -> toggleDisableHint());
        skipDuringPlay_checkbox.setOnAction(_ -> toggleSkipDuringPlay());

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

        setupTypingTimer(descriptionField, this::updateDescriptionInDB);
        setupTypingTimer(hintField, this::updateHintInDB);

        loadInstructions();
        populatePartSelector();
        partSelector.valueProperty().addListener((_, _, selectedPart) -> updatePartInDB((Part) selectedPart));
        partQuantiy_spinner.valueProperty().addListener((_, oldValue, newValue) -> {updateQuantityInDB(newValue);});

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

            // Disable listeners while updating UI
            isUpdatingUI = true;

            // Update UI fields (this won't trigger unnecessary DB updates now)
            descriptionField.setText(instruction.getDescription());
            hintField.setText(instruction.getHint() != null ? instruction.getHint() : "");
            disableHint_checkbox.setSelected(instruction.hasProperty(Instruction.InstructionProperty.HINT_DISABLED));
            skipDuringPlay_checkbox.setSelected(instruction.hasProperty(Instruction.InstructionProperty.SKIP_DURING_PLAY));

            // Store original values
            originalDescription = instruction.getDescription();
            originalHint = instruction.getHint() != null ? instruction.getHint() : "";

            // Handle Part Selection (only for PickingInstruction)
            if (instruction instanceof PickingInstruction pickingInstruction) {
                pickInstructionSettings_hbox.setDisable(false);
                partSelector.setValue(pickingInstruction.getPartToPick());
                originalPart = (pickingInstruction.getPartToPick());
                partQuantiy_spinner.getValueFactory().setValue(pickingInstruction.getQuantity());
            } else {
                pickInstructionSettings_hbox.setDisable(true);
                partSelector.setValue(null);
                originalPart = null;
            }

            // Load Image (if available)
            String imagePath = InstructionRepository.getInstance().getInstructionImagePath(instruction.getId());
            InstructionImageView.setImage(imagePath != null ? ImageHandler.loadImage(imagePath) : null);

            // Re-enable listeners after UI update is complete
            isUpdatingUI = false;
        }
    }

    private void addAssemblyInstruction() {
        if (selectedNode == null) {
            showErrorDialog("Error", "No instruction selected", "Please select an instruction before adding a sub-instruction.", "OK");
            return;
        }

        Instruction parentInstruction = selectedNode.getValue();
        if (!(parentInstruction instanceof AssemblyInstruction)) {
            showErrorDialog("Invalid Selection", "Cannot add a sub-instruction here", "You can only add sub-instructions to assembly instructions.", "OK");
            return;
        }

        AssemblyInstruction newInstruction = new AssemblyInstruction("New Assembly Instruction", assembly.getId(), parentInstruction.getId());
        InstructionRepository.getInstance().insertAssemblyInstruction(newInstruction);

        TreeItem<Instruction> subNode = new TreeItem<>(newInstruction);
        selectedNode.getChildren().add(subNode);
        selectedNode.setExpanded(true);
    }

    private void addPickingInstruction() {
        if (selectedNode == null) {
            showErrorDialog("Error", "No instruction selected", "Please select an instruction before adding a picking instruction.", "OK");
            return;
        }


        Instruction parentInstruction = selectedNode.getValue();
        if (!(parentInstruction instanceof AssemblyInstruction)) {
            showErrorDialog("Invalid Selection", "Cannot add a sub-instruction here", "You can only add sub-instructions to assembly instructions.", "OK");
            return;
        }

        PickingInstruction newInstruction = new PickingInstruction("New Picking Instruction", null, assembly.getId(), parentInstruction.getId());
        InstructionRepository.getInstance().insertPickingInstruction(newInstruction);

        TreeItem<Instruction> subNode = new TreeItem<>(newInstruction);
        selectedNode.getChildren().add(subNode);
        selectedNode.setExpanded(true);
    }

    private void removeSelectedInstruction() {
        if (selectedNode == null) {
            showInfoDialog("Warning", "No instruction selected", "Please select an instruction before removing.", "Close");
            return;
        }

        Instruction instruction = selectedNode.getValue();
        // Dont delete rootNode
        if (instruction.getParentInstructionId() != null) {
            InstructionRepository.getInstance().removeInstruction(instruction.getId());
            TreeItem<Instruction> parent = selectedNode.getParent();
            if (parent != null) {
                parent.getChildren().remove(selectedNode);
            } else {
                instructionTree.setRoot(null);
            }
        } else {
            showErrorDialog("Error", "Can't delete rootNode", "", "OK");
        }

        selectedNode = null;
    }

    private void loadInstructions() {
        if (assembly == null) {
            showErrorDialog("Error", "No assembly selected", "Please select an assembly before loading instructions.", "OK");
            return;
        }

        // Store the ID of the selected instruction
        Long selectedInstructionId = (selectedNode != null) ? selectedNode.getValue().getId() : null;

        ArrayList<Instruction> instructions = (ArrayList<Instruction>) InstructionRepository.getInstance().findByAssembly(assembly);
        Map<Long, TreeItem<Instruction>> itemMap = new HashMap<>();
        TreeItem<Instruction> root = null;
        TreeItem<Instruction> nodeToSelect = null;

        for (Instruction instruction : instructions) {
            TreeItem<Instruction> item = new TreeItem<>(instruction);
            itemMap.put(instruction.getId(), item);

            if (instruction.getParentInstructionId() == null) {
                if (root != null) {
                    showErrorDialog("Error", "Multiple root instructions found", "Please check database integrity.", "OK");
                    return;
                }
                root = item;
            }

            // Check if this is the previously selected instruction
            if (instruction.getId().equals(selectedInstructionId)) {
                nodeToSelect = item;
            }
        }

        if (root == null) {
            AssemblyInstruction rootInstruction = new AssemblyInstruction(assembly.getName(), assembly.getId(), null);
            InstructionRepository.getInstance().insertAssemblyInstruction(rootInstruction);
            root = new TreeItem<>(rootInstruction);
            itemMap.put(rootInstruction.getId(), root);
            instructions.add(rootInstruction);
        }

        for (Instruction instruction : instructions) {
            if (instruction.getParentInstructionId() != null) {
                TreeItem<Instruction> parent = itemMap.get(instruction.getParentInstructionId());
                if (parent != null) {
                    parent.getChildren().add(itemMap.get(instruction.getId()));
                    parent.setExpanded(true);
                }
            }
        }

        instructionTree.setRoot(root);
        instructionTree.setShowRoot(true);

        // Restore selection if possible
        if (nodeToSelect != null) {
            instructionTree.getSelectionModel().select(nodeToSelect);
            selectedNode = nodeToSelect;
        }
    }

    private void toggleDisableHint() {
        boolean isDisabled = disableHint_checkbox.isSelected();
        if (selectedNode == null) return;
        if (isDisabled) {
            hintField.setDisable(true);

            Instruction selectedInstruction = selectedNode.getValue();
            selectedInstruction.setHint(null);
            selectedInstruction.addProperty(Instruction.InstructionProperty.HINT_DISABLED);
            InstructionRepository.getInstance().updateInstruction(selectedNode.getValue());

            System.out.println("Disabled Hints for: " + selectedInstruction);
        } else {
            hintField.setDisable(false);

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
            if (selectedNode != null && oldValue != null && !oldValue.equals(newValue) && !isUpdatingUI) {
                typingTimer.stop();
                typingTimer.playFromStart();
            }
        });
    }

    private void updateDescriptionInDB() {
        if (selectedNode == null) return;

        Instruction instruction = selectedNode.getValue();
        if (instruction == null) return;

        String newValue = descriptionField.getText();
        if (!Objects.equals(originalDescription, newValue)) {
            instruction.setDescription(newValue);
            InstructionRepository.getInstance().updateInstruction(instruction);
            originalDescription = newValue;
            System.out.println("Updated description in DB: " + newValue);
        }
        loadInstructions();
    }

    private void updateHintInDB() {
        if (selectedNode == null) return;

        Instruction instruction = selectedNode.getValue();
        if (instruction == null) return;

        String newValue = hintField.getText();
        if (!Objects.equals(originalHint, newValue)) {
            instruction.setHint(newValue);
            InstructionRepository.getInstance().updateInstruction(instruction);
            originalHint = newValue;
            System.out.println("Updated hint in DB: " + newValue);
        }
        loadInstructions();
    }

    private void updatePartInDB(Part selectedPart) {
        if (selectedNode == null) return;

        Instruction instruction = selectedNode.getValue();
        if (!(instruction instanceof PickingInstruction pickingInstruction)) return;

        if (!Objects.equals(originalPart, selectedPart)  && !isUpdatingUI) {
            pickingInstruction.setPartToPick(selectedPart);
            InstructionRepository.getInstance().updateInstruction(instruction);
            originalPart = selectedPart;
            System.out.println("Updated part in DB: " + selectedPart);
        }
        loadInstructions();
    }

    private void updateQuantityInDB(int newQuantity) {
        if (selectedNode == null) return;

        Instruction instruction = selectedNode.getValue();
        if (!(instruction instanceof PickingInstruction pickingInstruction)) return;

        if (pickingInstruction.getQuantity() != newQuantity && !isUpdatingUI) {
            pickingInstruction.setQuantity(newQuantity);
            InstructionRepository.getInstance().updateInstruction(pickingInstruction);
            System.out.println("Updated quantity in DB: " + newQuantity);
        }
    }


    private void uploadInstructionImage() {
        try {
            Image image = uploadImage(InstructionImageView.getScene().getWindow());
            InstructionRepository.getInstance().uploadImageToInstruction(image.getUrl(), selectedNode.getValue().getId());
        } catch (IOException e) {
            showExceptionDialog("Error while uploading Image", e);
        }
    }
    private void deleteImage() {InstructionImageView.setImage(null);}

    @Override
    public void cleanup() {

    }
}
