package be.uhasselt.dwi_application.utility.modules;

import be.uhasselt.dwi_application.model.basic.Position;
import be.uhasselt.dwi_application.utility.database.repository.settings.SettingsRepository;

public class ConvertToStripCoords {
    public static Position convertToStripCoords(Position worldPosition) {
        int gridSize = SettingsRepository.loadSettings().getGridSize() / 2;
        return new Position(43 - worldPosition.getX() / gridSize, (worldPosition.getY() / gridSize));
    }
}
