package be.uhasselt.dwi_application.controller.WorkInstruction.Manager;

import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.model.workInstruction.AssemblyInstruction;
import be.uhasselt.dwi_application.model.workInstruction.Instruction;
import be.uhasselt.dwi_application.model.workInstruction.PickingInstruction;
import be.uhasselt.dwi_application.utility.FxmlViews;
import be.uhasselt.dwi_application.utility.database.repository.instruction.InstructionRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.io.IOException;

import static be.uhasselt.dwi_application.utility.modules.Dialog.showErrorDialog;
import static be.uhasselt.dwi_application.utility.modules.Dialog.showInfoDialog;

public class InstructionTreeItemController extends TreeCell<Instruction> {
    @FXML private Label instructionLabel;
    @FXML private TextField instructionTextField;
    @FXML private HBox InstructionButtons_hbox;
    @FXML private Button addSubInstruction_btn;
    @FXML private Button deleteInstruction_btn;
    @FXML private ComboBox<String> AddInstructionType_combo;

    private final HBox root;
    private final Assembly assembly;

    public InstructionTreeItemController(Assembly assembly) {
        this.assembly = assembly;

        // Load Instruction Tree Item
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FxmlViews.INSTRUCTION_TREE_ITEM));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load InstructionTreeCell.fxml", e);
        }

        // Instruction Description
        instructionLabel.setOnMouseClicked(this::startEditing);
        instructionTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                saveInstructionName();
            }
        });
        instructionTextField.focusedProperty().addListener((_, _, isNowFocused) -> {
            if (!isNowFocused) {
                saveInstructionName();
            }
        });

        // Instruction Management Buttons
        InstructionButtons_hbox.setVisible(false);
        deleteInstruction_btn.setOnAction(_ -> deleteInstruction());

        this.setOnMouseEntered(e -> InstructionButtons_hbox.setVisible(true));
        this.setOnMouseExited(e -> {
            InstructionButtons_hbox.setVisible(false);
            AddInstructionType_combo.setVisible(false);
            AddInstructionType_combo.setManaged(false);
        });

        AddInstructionType_combo.setItems(FXCollections.observableArrayList("Assembly Instruction", "Picking Instruction"));
        AddInstructionType_combo.setVisible(false);
        InstructionButtons_hbox.setSpacing(5);

        addSubInstruction_btn.setOnAction(e -> {
            AddInstructionType_combo.setVisible(true);
            AddInstructionType_combo.setManaged(true);
            AddInstructionType_combo.show();
        });

        AddInstructionType_combo.setOnAction(e -> {
            String selectedType = AddInstructionType_combo.getValue();
            if (selectedType != null) {
                addSubInstruction(getItem(), selectedType);
                AddInstructionType_combo.setVisible(false);
                AddInstructionType_combo.setManaged(false);
            }
        });
    }

    private void startEditing(MouseEvent event) {
        Instruction instruction = getItem();
        if (instruction != null) {
            instructionTextField.setText(instruction.getDescription());
            toggleEditMode(true);
            instructionTextField.requestFocus();
            instructionTextField.selectAll();
        }
    }

    private void saveInstructionName() {
        Instruction instruction = getItem();
        if (instruction != null) {
            String newName = instructionTextField.getText().trim();
            if (!newName.isEmpty() && !newName.equals(instruction.getDescription())) {
                instruction.setDescription(newName);
                InstructionRepository.getInstance().updateInstruction(instruction); // Update in database
            }
            instructionLabel.setText(newName);
        }
        toggleEditMode(false);
    }

    private void toggleEditMode(boolean editMode) {
        instructionLabel.setVisible(!editMode);
        instructionLabel.setManaged(!editMode);
        instructionTextField.setVisible(editMode);
        instructionTextField.setManaged(editMode);
    }

    private void deleteInstruction() {
        Instruction instruction = getItem();
        TreeItem<Instruction> selectedNode = getTreeItem();

        // Dont delete rootNode
        if (instruction.getParentInstructionId() != null) {
            InstructionRepository.getInstance().delete(instruction.getId());
            TreeItem<Instruction> parent = selectedNode.getParent();

            if (parent != null) {
                parent.getChildren().remove(selectedNode);
            }

        } else {
            showErrorDialog("Error", "Can't delete rootNode", "", "OK");
        }
    }

    private void addSubInstruction(Instruction item, String type) {
        if (!(item instanceof AssemblyInstruction)) {
            showErrorDialog("Invalid Selection", "Cannot add a sub-instruction here",
                    "You can only add sub-instructions to assembly instructions.", "OK");
            return;
        }

        Instruction newInstruction = null;

        if (type.equals("Assembly Instruction")) {
            newInstruction = new AssemblyInstruction("New Assembly Instruction", assembly.getId(), item.getId());
            InstructionRepository.getInstance().insertAssemblyInstruction((AssemblyInstruction) newInstruction);
        }
        else if (type.equals("Picking Instruction")) {
            newInstruction = new PickingInstruction("New Picking Instruction", null, assembly.getId(), item.getId());
            InstructionRepository.getInstance().insertPickingInstruction((PickingInstruction) newInstruction);
        }

        TreeItem<Instruction> subNode = new TreeItem<>(newInstruction);
        getTreeItem().getChildren().add(subNode);
        getTreeItem().setExpanded(true);
        getTreeView().refresh();
        System.out.println("Added sub-instruction: " + (newInstruction != null ? newInstruction.getDescription() : "Description == null"));
    }

    @Override
    protected void updateItem(Instruction item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            instructionLabel.setText(item.getDescription());
            setGraphic(root);
        }
    }
}
