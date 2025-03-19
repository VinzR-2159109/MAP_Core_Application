package be.uhasselt.dwi_application.controller.WorkInstruction.Manager.InstructionTreeItems;

import be.uhasselt.dwi_application.model.workInstruction.Instruction;
import be.uhasselt.dwi_application.utility.database.repository.instruction.InstructionRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static be.uhasselt.dwi_application.utility.modules.Dialog.showErrorDialog;

public abstract class instructionTreeItemController extends TreeCell<Instruction> {
    @FXML protected Label instructionLabel;
    @FXML protected TextField instructionTextField;
    @FXML protected HBox InstructionButtons_hbox;
    @FXML protected Button deleteInstruction_btn;

    protected final HBox root;

    public instructionTreeItemController(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
        }

        deleteInstruction_btn.setVisible(false);
        InstructionButtons_hbox.setSpacing(5);

        setupEventListeners();
    }

    private void setupEventListeners() {
        instructionLabel.setOnMouseClicked(this::startEditing);
        instructionTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) saveInstructionName();
        });
        instructionTextField.focusedProperty().addListener((_, _, isNowFocused) -> {
            if (!isNowFocused) saveInstructionName();
        });

        deleteInstruction_btn.setOnAction(_ -> deleteInstruction());

        this.setOnMouseEntered(e -> deleteInstruction_btn.setVisible(true));
        this.setOnMouseExited(e -> deleteInstruction_btn.setVisible(false));
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
                InstructionRepository.getInstance().updateInstruction(instruction);
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
        if (instruction == null || instruction.getParentInstructionId() == null) {
            showErrorDialog("Error", "Can't delete rootNode", "", "OK");
            return;
        }

        InstructionRepository.getInstance().delete(instruction.getId());
        TreeItem<Instruction> parent = getTreeItem().getParent();
        if (parent != null) {
            parent.getChildren().remove(getTreeItem());
        }
    }

    @Override
    protected void updateItem(Instruction item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }
        System.out.println("Doing stuff here");
        instructionLabel.setText(item.getDescription());
        setGraphic(root);
    }
}
