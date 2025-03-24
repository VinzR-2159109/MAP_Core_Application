package be.uhasselt.dwi_application.controller.WorkInstruction.Manager;

import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.model.workInstruction.AssemblyInstruction;
import be.uhasselt.dwi_application.model.workInstruction.Instruction;
import be.uhasselt.dwi_application.model.workInstruction.picking.PickingInstruction;
import be.uhasselt.dwi_application.utility.FxmlViews;
import be.uhasselt.dwi_application.utility.database.repository.instruction.InstructionRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.IOException;

import static be.uhasselt.dwi_application.utility.modules.Dialog.showErrorDialog;

public class InstructionTreeItemController extends TreeCell<Instruction> {
    @FXML private Label instructionLabel;
    @FXML private TextField instructionTextField;
    @FXML private HBox InstructionButtons_hbox;
    @FXML private Button addSubInstruction_btn;
    @FXML private Button deleteInstruction_btn;
    @FXML private ComboBox<String> addInstructionType_combo;

    private final HBox root;
    private final Assembly assembly;

    public InstructionTreeItemController(Assembly assembly) {
        this.assembly = assembly;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FxmlViews.INSTRUCTION_TREE_ITEM));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load InstructionTreeCell.fxml", e);
        }

        instructionLabel.setOnMouseClicked(event-> {
            if (event.getClickCount() == 2){
                startEditing();
            }
        });
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
        addSubInstruction_btn.setVisible(false);
        deleteInstruction_btn.setVisible(false);
        addInstructionType_combo.setVisible(false);
        addInstructionType_combo.setManaged(false);
        addInstructionType_combo.setDisable(true);

        deleteInstruction_btn.setOnAction(_ -> deleteInstruction());

        this.setOnMouseEntered(e -> {
            if (getItem() instanceof AssemblyInstruction) {
                addSubInstruction_btn.setVisible(true);
            }
            deleteInstruction_btn.setVisible(true);
        });
        this.setOnMouseExited(e -> {
            addSubInstruction_btn.setVisible(false);
            deleteInstruction_btn.setVisible(false);

            addInstructionType_combo.setDisable(false);
            addInstructionType_combo.setVisible(false);
            addInstructionType_combo.setManaged(false);
        });

        addInstructionType_combo.setItems(FXCollections.observableArrayList("Assembly Instruction", "Picking Instruction"));
        addInstructionType_combo.setVisible(false);
        InstructionButtons_hbox.setSpacing(5);

        addSubInstruction_btn.setOnAction(e -> {
            addInstructionType_combo.setVisible(true);
            addInstructionType_combo.setManaged(true);
            addInstructionType_combo.show();
        });

        addInstructionType_combo.setOnAction(e -> {
            String selectedType = addInstructionType_combo.getValue();
            if (selectedType != null) {
                addSubInstruction(getItem(), selectedType);
                addInstructionType_combo.setVisible(false);
                addInstructionType_combo.setManaged(false);
            }
        });
    }

    private void startEditing() {
        Instruction instruction = getItem();

        instructionTextField.setText(instruction.getDescription());
        toggleEditMode(true);
        instructionTextField.requestFocus();
        instructionTextField.selectAll();

        instructionTextField.setPrefWidth(new Text(instructionTextField.getText()).getLayoutBounds().getWidth() + 10);
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
            return;
        }

        if (item instanceof AssemblyInstruction) {
            instructionLabel.setText(item.getDescription());
            instructionLabel.setStyle("-fx-text-fill: black;");
        }
        else if (item instanceof PickingInstruction) {
            instructionLabel.setText("📦 " + item.getDescription());
            instructionLabel.setStyle("-fx-text-fill: green;");
        }
        setGraphic(root);
    }
}
