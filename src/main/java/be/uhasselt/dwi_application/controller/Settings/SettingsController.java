package be.uhasselt.dwi_application.controller.Settings;

import be.uhasselt.dwi_application.utility.database.repository.settings.Settings;
import be.uhasselt.dwi_application.utility.database.repository.settings.SettingsRepository;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import java.util.ArrayList;
import java.util.List;

public class SettingsController {
    @FXML private CheckBox haptic_check;
    @FXML private CheckBox liveLight_check;
    @FXML private CheckBox staticLight_check;
    @FXML private CheckBox flowLight_check;
    @FXML private CheckBox gradientLight_check;

    @FXML private CheckBox assemblyAssistance_check;

    @FXML private Spinner<Integer> necessaryQOW_spinner;
    @FXML private Spinner<Integer> videoEnlargementFactor_spinner;

    @FXML private Spinner<Integer> LEDXLength_spinner;
    @FXML private Spinner<Integer> LEDYLength_spinner;

    @FXML private Slider staticBrightness_slider;

    private final ArrayList<CheckBox> checkedBoxes = new ArrayList<>();
    private final Settings settings = SettingsRepository.loadSettings();

    @FXML
    private void initialize() {
        setupCheckbox(haptic_check, Settings.EnabledAssistanceSystem.HAPTIC);
        setupCheckbox(liveLight_check, Settings.EnabledAssistanceSystem.LIVE_LIGHT);
        setupCheckbox(staticLight_check, Settings.EnabledAssistanceSystem.STATIC_LIGHT);
        setupCheckbox(flowLight_check, Settings.EnabledAssistanceSystem.FLOW_LIGHT);
        setupCheckbox(gradientLight_check, Settings.EnabledAssistanceSystem.GRADIENT_LIGHT);

        setupAssemblyAssistanceCheckbox();

        setupQOWSpinner();
        setupVideoEnlargementSpinner();
        setupLEDLengthSpinners();

        setupBrightnessSlider();
    }

    private void setupAssemblyAssistanceCheckbox() {
        assemblyAssistance_check.setSelected(settings.isAssemblyAssistanceEnabled());

        assemblyAssistance_check.selectedProperty().addListener((_, _, newValue) -> {
            settings.setAssemblyAssistanceEnabled(newValue);
            SettingsRepository.updateSettings(settings);
        });
    }

    private void setupBrightnessSlider() {
        staticBrightness_slider.setValue(settings.getStaticBrightness());

        staticBrightness_slider.valueProperty().addListener((_, _, newVal) -> {
            settings.setStaticBrightness(newVal.intValue());
            SettingsRepository.updateSettings(settings);
        });
    }


    private void setupCheckbox(CheckBox checkBox, Settings.EnabledAssistanceSystem system) {
        if (settings.getEnabledAssistanceSystemsAsList().contains(system)) {
            checkBox.setSelected(true);
            checkedBoxes.add(checkBox);
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
        LEDXLength_spinner.setValueFactory(valueFactory);
        LEDYLength_spinner.setValueFactory(valueFactory);

        LEDXLength_spinner.getValueFactory().setValue(settings.getXLEDLength());
        LEDYLength_spinner.getValueFactory().setValue(settings.getYLEDLength());

        LEDXLength_spinner.valueProperty().addListener((_, _, newVal) -> {
            settings.setXLEDLength(newVal);
            SettingsRepository.updateSettings(settings);
        });

        LEDYLength_spinner.valueProperty().addListener((_, _, newVal) -> {
            settings.setYLEDLength(newVal);
            SettingsRepository.updateSettings(settings);
        });
    }

    private void updateSettings() {
        List<Settings.EnabledAssistanceSystem> enabledAssistanceSystems = new ArrayList<>();

        for (CheckBox checkBox : checkedBoxes) {
            if (checkBox.isSelected()) {
                Settings.EnabledAssistanceSystem system = switch (checkBox.getText().toLowerCase()) {
                    case "haptic" -> Settings.EnabledAssistanceSystem.HAPTIC;
                    case "live light" -> Settings.EnabledAssistanceSystem.LIVE_LIGHT;
                    case "static light" -> Settings.EnabledAssistanceSystem.STATIC_LIGHT;
                    case "flow light" -> Settings.EnabledAssistanceSystem.FLOW_LIGHT;
                    case "gradient light" -> Settings.EnabledAssistanceSystem.GRADIENT_LIGHT;
                    default -> throw new IllegalArgumentException("Unknown checkbox text: " + checkBox.getText());
                };
                enabledAssistanceSystems.add(system);
            }
        }

        settings.setEnabledAssistanceSystemsFromList(enabledAssistanceSystems);
        SettingsRepository.updateSettings(settings);

        System.out.println("Updated enabled systems: " + enabledAssistanceSystems);
    }
}
