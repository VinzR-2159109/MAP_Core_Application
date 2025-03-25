package be.uhasselt.dwi_application.controller.WorkInstruction.Assembly;

import be.uhasselt.dwi_application.controller.AssemblyPlayer.AssemblyPlayerController;
import be.uhasselt.dwi_application.controller.Controller;
import be.uhasselt.dwi_application.controller.MainController;
import be.uhasselt.dwi_application.controller.WorkInstruction.Manager.InstructionManagerController;
import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.utility.FxmlViews;
import be.uhasselt.dwi_application.utility.database.repository.assembly.AssemblyRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;

import static be.uhasselt.dwi_application.utility.modules.Dialog.showExceptionDialog;

public class AssemblyTileController implements Controller {
    @FXML private VBox tileContainer_vbox;
    @FXML private Button editAssembly_btn;
    @FXML private CheckBox selectCheckBox;
    @FXML private Label assemblyName_lbl;
    @FXML private TextField assemblyName_txt;
    @FXML private Rectangle colorBox;
    @FXML private StackPane colorBoxStack;
    @FXML private Label playIcon;

    private final Assembly assembly;
    private final Consumer<Assembly> onSelectionChange;
    private final AssemblyRepository assemblyRepository = AssemblyRepository.getInstance();

    public AssemblyTileController(Assembly assembly, Consumer<Assembly> onSelectionChange) {
        this.assembly = assembly;
        this.onSelectionChange = onSelectionChange;
    }

    @FXML
    public void initialize() {
        // Set assembly name
        assemblyName_lbl.setText(assembly.getName());
        assemblyName_txt.setText(assembly.getName());

        colorBox.setFill(assembly.getColor());

        // Set up event handlers
        assemblyName_lbl.setOnMouseClicked( event -> {
            enterEditName();
            event.consume();
        });

        tileContainer_vbox.setOnMouseClicked(_ -> playAssembly());
        selectCheckBox.setOnAction(_ -> onSelectionChange.accept(assembly));
        editAssembly_btn.setOnAction(_ -> editAssembly());

        assemblyName_txt.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                saveAssemblyName();
            }
        });

        assemblyName_txt.focusedProperty().addListener((_, _, isNowFocused) -> {
            if (!isNowFocused) {
                saveAssemblyName();
            }
        });

        colorBoxStack.setOnMouseEntered(e -> playIcon.setVisible(true));
        colorBoxStack.setOnMouseExited(e -> playIcon.setVisible(false));

    }

    private void playAssembly() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FxmlViews.ASSEMBLY_PLAYER));
            loader.setControllerFactory(_ -> new AssemblyPlayerController(assembly));
            Parent assemblyPlayer = loader.load();
            Controller controller = loader.getController();
            MainController.getInstance().setContentView(assemblyPlayer, controller);
        } catch (Exception e) {
            e.printStackTrace();
            showExceptionDialog("Fault in playAssembly", e);
        }
    }

    private void editAssembly() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FxmlViews.INSTRUCTION_CREATER));
            loader.setControllerFactory(_-> new InstructionManagerController(assembly));
            Parent instructionView = loader.load();
            Controller controller = loader.getController();

            MainController.getInstance().setContentView(instructionView, controller);
        } catch (Exception e) {
            e.printStackTrace();
            showExceptionDialog("Fault in handleTileClick", e);
        }
    }

    private void enterEditName() {
        assemblyName_lbl.setVisible(false);
        assemblyName_txt.setVisible(true);
        assemblyName_txt.requestFocus();
    }

    private void saveAssemblyName() {
        String newName = assemblyName_txt.getText().trim();
        if (!newName.isEmpty() && !newName.equals(assembly.getName())) {
            assembly.setName(newName);
            assemblyRepository.update(assembly);
            System.out.println("Updated Assembly Name: " + newName);
        }

        // Reset UI to non-edit mode
        assemblyName_lbl.setText(assembly.getName());
        assemblyName_lbl.setVisible(true);
        assemblyName_txt.setVisible(false);
        editAssembly_btn.setText("Edit");
    }

    void updateColor(Color selectedColor) {
        colorBox.setFill(assembly.getColor());
    }

    @Override
    public void cleanup() {

    }
}
