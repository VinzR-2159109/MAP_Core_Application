package be.uhasselt.dwi_application.utility.database.repository.settings;

import be.uhasselt.dwi_application.utility.database.Database;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

public class SettingsRepository {

    private static final SettingsRepository instance = new SettingsRepository();
    private final SettingsDao settingsDAO;

    private SettingsRepository() {
        Jdbi jdbi = Database.getJdbi();
        this.settingsDAO = jdbi.onDemand(SettingsDao.class);
    }

    public static Settings loadSettings() {
        return instance._loadSettings();
    }

    public static void updateSettings(Settings settings) {
        instance._updateSettings(settings);
    }

    private void _updateSettings(Settings updatedSettings) {
        Settings settings = _loadSettings();
        updatedSettings.setId(settings.getId());
        settingsDAO.updateSettings(updatedSettings.getId(), updatedSettings.getGridSize(), updatedSettings.getEnabledAssistanceSystemsAsString(), updatedSettings.getNecessaryQOW());
    }

    private Settings _loadSettings() {
        return Optional.ofNullable(settingsDAO.getSettings())
                .orElseGet(this::createDefaultSettings);
    }

    private Settings createDefaultSettings() {
        Settings newSettings = new Settings();
        Long generatedId = settingsDAO.insertSettings(newSettings);
        newSettings.setId(generatedId);
        return newSettings;
    }
}
