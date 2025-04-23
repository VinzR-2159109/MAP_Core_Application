package be.uhasselt.dwi_application.controller.Settings;

import be.uhasselt.dwi_application.utility.database.repository.settings.Settings;
import be.uhasselt.dwi_application.utility.database.repository.settings.SettingsRepository;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import java.util.ArrayList;
import java.util.List;

public class SettingsController {
    @FXML private CheckBox haptic_check;
    @FXML private CheckBox liveLight_check;
    @FXML private CheckBox staticLight_check;
    @FXML private CheckBox flowLight_check;

    @FXML private Spinner<Integer> necessaryQOW_spinner;
    @FXML private Spinner<Integer> videoEnlargementFactor_spinner;

    @FXML private Spinner<Integer> LEDXLenght_spinner;
    @FXML private Spinner<Integer> LEDYLenght_spinner;

    private ArrayList<CheckBox> checkedBoxes = new ArrayList<>();
    private Settings settings = SettingsRepository.loadSettings();

    @FXML
    private void initialize() {
        setupCheckbox(haptic_check);
        setupCheckbox(liveLight_check);
        setupCheckbox(staticLight_check);
        setupCheckbox(flowLight_check);

        setupQOWSpinner();
        setupVideoEnlargementSpinner();
        setupLEDLengthSpinners();
    }

    private void setupCheckbox(CheckBox checkBox) {
        for (Settings.EnabledAssistanceSystem enabledSystem : settings.getEnabledAssistanceSystemsAsList()){
            switch (enabledSystem.name().toLowerCase()){
                case "haptic":
                    haptic_check.setSelected(true);
                    checkedBoxes.add(checkBox);
                    break;
                case "live_light": liveLight_check.setSelected(true);
                    checkedBoxes.add(checkBox);
                    break;
                case "static_light": staticLight_check.setSelected(true);
                    checkedBoxes.add(checkBox);
                    break;
                case "flow_light": flowLight_check.setSelected(true);
                    checkedBoxes.add(checkBox);
                    break;
            }
        }

        checkBox.selectedProperty().addListener((_, _, newValue) -> {
            if (newValue) {
                if (!checkedBoxes.contains(checkBox)) {
                    checkedBoxes.add(checkBox);
                }
            } else {
                checkedBoxes.remove(checkBox);
            }
            updateSettings();
        });
    }


    private void setupQOWSpinner() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(50, 100);
        necessaryQOW_spinner.setValueFactory(valueFactory);

        necessaryQOW_spinner.getValueFactory().setValue(settings.getNecessaryQOW());

        necessaryQOW_spinner.valueProperty().addListener((_, _, newVal) -> {
            settings.setNecessaryQOW(newVal);
            SettingsRepository.updateSettings(settings);
        });
    }

    private void setupVideoEnlargementSpinner() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5);
        videoEnlargementFactor_spinner.setValueFactory(valueFactory);

        videoEnlargementFactor_spinner.getValueFactory().setValue(settings.getVideoEnlargementFactor());

        videoEnlargementFactor_spinner.valueProperty().addListener((_, _, newVal) -> {
            settings.setVideoEnlargementFactor(newVal);
            SettingsRepository.updateSettings(settings);
        });

    }

    private void setupLEDLengthSpinners() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100);
        LEDXLenght_spinner.setValueFactory(valueFactory);
        LEDYLenght_spinner.setValueFactory(valueFactory);

        LEDXLenght_spinner.getValueFactory().setValue(settings.getXLEDLength());
        LEDYLenght_spinner.getValueFactory().setValue(settings.getYLEDLength());

        LEDXLenght_spinner.valueProperty().addListener((_, _, newVal) -> {
            settings.setYLEDLenght(newVal);
            SettingsRepository.updateSettings(settings);
        });

        LEDYLenght_spinner.valueProperty().addListener((_, _, newVal) -> {
            settings.setYLEDLenght(newVal);
            SettingsRepository.updateSettings(settings);
        });
    }

    private void updateSettings() {
        Settings settings = SettingsRepository.loadSettings();
        List<Settings.EnabledAssistanceSystem> enabledAssistanceSystems = new ArrayList<>();

        for (CheckBox checkBox : checkedBoxes) {
            if (checkBox.isSelected()) {
                Settings.EnabledAssistanceSystem system = switch (checkBox.getText().toLowerCase()) {
                    case "haptic" -> Settings.EnabledAssistanceSystem.HAPTIC;
                    case "live light" -> Settings.EnabledAssistanceSystem.LIVE_LIGHT;
                    case "static light" -> Settings.EnabledAssistanceSystem.STATIC_LIGHT;
                    case "flow light" -> Settings.EnabledAssistanceSystem.FLOW_LIGHT;
                    default -> throw new IllegalArgumentException("Unknown checkbox text: " + checkBox.getText());
                };
                enabledAssistanceSystems.add(system);
            }
        }

        settings.setEnabledAssistanceSystemsFromList(enabledAssistanceSystems);
        SettingsRepository.updateSettings(settings);
    }

}
