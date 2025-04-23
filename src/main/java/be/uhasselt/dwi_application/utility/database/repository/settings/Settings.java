package be.uhasselt.dwi_application.utility.database.repository.settings;

import java.util.ArrayList;
import java.util.List;

public class Settings {
    private Long id;
    private int gridSize;
    private List<EnabledAssistanceSystem> enabledAssistanceSystems;
    private int necessaryQOW;
    private int videoEnlargementFactor;
    private int XLEDLength;
    private int yLEDLenght;

    public enum EnabledAssistanceSystem {
        HAPTIC, LIVE_LIGHT, STATIC_LIGHT, FLOW_LIGHT
    }
    public Settings() {
       this(40, List.of(EnabledAssistanceSystem.STATIC_LIGHT), 85, 2, 43, 28);
    }

    private Settings(int gridSize, List<EnabledAssistanceSystem> enabledAssistanceSystems, int necessaryQOW, int videoEnlargementFactor, int XLEDLenght, int YLEDLenght) {
        this.gridSize = gridSize;
        this.enabledAssistanceSystems = enabledAssistanceSystems;
        this.necessaryQOW = necessaryQOW;
        this.videoEnlargementFactor = videoEnlargementFactor;
        this.XLEDLength = XLEDLenght;
        this.yLEDLenght = YLEDLenght;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setGridSize(int gridSize) {this.gridSize = gridSize;}
    public int getGridSize(){return gridSize;}

    public void setEnabledAssistanceSystemsFromList(List<EnabledAssistanceSystem> enabledAssistanceSystems) {this.enabledAssistanceSystems = new ArrayList<>(enabledAssistanceSystems);}
    public List<EnabledAssistanceSystem> getEnabledAssistanceSystemsAsList() {
        return enabledAssistanceSystems;
    }
    public String getEnabledAssistanceSystems() {
        return getEnabledAssistanceSystemsAsString();
    }
    public void setEnabledAssistanceSystems(String systemsString) {setEnabledAssistanceSystemsFromString(systemsString);}
    public String getEnabledAssistanceSystemsAsString() {
        return enabledAssistanceSystems.stream()
                .map(Enum::name)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }
    public void setEnabledAssistanceSystemsFromString(String systemsString) {
        if (systemsString == null || systemsString.isBlank()) {
            this.enabledAssistanceSystems = new ArrayList<>();
            return;
        }

        String[] parts = systemsString.split(",");
        List<EnabledAssistanceSystem> systems = new ArrayList<>();
        for (String part : parts) {
            systems.add(EnabledAssistanceSystem.valueOf(part.trim()));
        }
        this.enabledAssistanceSystems = systems;
    }

    public void setNecessaryQOW(int newNecessaryQOW) {this.necessaryQOW = newNecessaryQOW;}
    public int getNecessaryQOW() {return necessaryQOW;}

    public void setVideoEnlargementFactor(int newVideoEnlargementFactor) {this.videoEnlargementFactor = newVideoEnlargementFactor;}
    public int getVideoEnlargementFactor() {return videoEnlargementFactor;}

    public int getXLEDLength() {return XLEDLength;}
    public void setXLEDLength(int LEDLength) {this.XLEDLength = LEDLength;}

    public int getYLEDLength() {return yLEDLenght;}
    public void setYLEDLenght(int yLEDLenght) {this.yLEDLenght = yLEDLenght;}

    public Long getId() {
        return id;
    }
}
