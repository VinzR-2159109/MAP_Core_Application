package be.uhasselt.dwi_application.utility.database.repository.settings;

public class Settings {
    private Long id;
    private int gridSize;

    public Settings() {
       this(40);
    }

    public Settings(int gridSize) {
        this.gridSize = gridSize;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setGridSize(int gridSize) {this.gridSize = gridSize;}
    public int getGridSize(){return gridSize;}

    public Long getId() {
        return id;
    }
}
