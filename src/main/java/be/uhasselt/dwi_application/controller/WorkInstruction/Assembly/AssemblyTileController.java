package be.uhasselt.dwi_application.controller.WorkInstruction.Assembly;

import be.uhasselt.dwi_application.controller.AssemblyPlayer.AssemblyPlayerController;
import be.uhasselt.dwi_application.controller.AssemblyPlayer.AssemblyPlayerManager;
import be.uhasselt.dwi_application.controller.Controller;
import be.uhasselt.dwi_application.controller.MainController;
import be.uhasselt.dwi_application.controller.WorkInstruction.InstructionManagerController;
import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.utility.FxmlViews;
import be.uhasselt.dwi_application.utility.database.repository.assembly.AssemblyRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

import static be.uhasselt.dwi_application.utility.modules.Dialog.showExceptionDialog;

public class AssemblyTileController implements Controller {
    @FXML private VBox tileContainer_vbox;
    @FXML private Button editSave_btn;
    @FXML private CheckBox selectCheckBox;
    @FXML private Label assemblyName_lbl;
    @FXML private TextField assemblyName_txt;
    @FXML private Button playAssembly_btn;

    private Assembly assembly;
    private Consumer<Assembly> onSelectionChange;
    private final AssemblyRepository assemblyRepository = AssemblyRepository.getInstance();

    public void setAssembly(Assembly assembly, Consumer<Assembly> onSelectionChange) {
        this.assembly = assembly;
        this.onSelectionChange = onSelectionChange;

        // Set assembly name
        assemblyName_lbl.setText(assembly.getName());
        assemblyName_txt.setText(assembly.getName());

        // Set up event handlers
        tileContainer_vbox.setOnMouseClicked(this::handleTileClick);
        selectCheckBox.setOnAction(event -> handleSelectionChange());
        editSave_btn.setOnAction(event -> toggleEditSave());
        playAssembly_btn.setOnAction(event -> playAssembly());
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

    private void handleTileClick(MouseEvent event) {
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

    private void handleSelectionChange() {
        onSelectionChange.accept(assembly);
    }

    @FXML
    private void toggleEditSave() {
        if (assemblyName_txt.isVisible()) {
            saveAssemblyName();
        } else {
            assemblyName_lbl.setVisible(false);
            assemblyName_txt.setVisible(true);
            assemblyName_txt.requestFocus();
            editSave_btn.setText("Save");
        }
    }

    private void saveAssemblyName() {
        String newName = assemblyName_txt.getText().trim();
        if (!newName.isEmpty() && !newName.equals(assembly.getName())) {
            assembly.setName(newName);
            assemblyRepository.updateAssembly(assembly);
            System.out.println("Updated Assembly Name: " + newName);
        }

        // Reset UI to non-edit mode
        assemblyName_lbl.setText(assembly.getName());
        assemblyName_lbl.setVisible(true);
        assemblyName_txt.setVisible(false);
        editSave_btn.setText("Edit");
    }

    @Override
    public void cleanup() {

    }
}
