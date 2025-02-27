package be.uhasselt.dwi_application.model.workInstruction;

import be.uhasselt.dwi_application.model.picking.Part;
import be.uhasselt.dwi_application.utility.database.repository.part.PartRepository;
import javafx.scene.paint.Color;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.util.List;

public class Assembly {
    private Long id;
    private String name;
    private String color;

    @JdbiConstructor
    public Assembly(@ColumnName("id") Long id, @ColumnName("name") String name, @ColumnName("color") String color) {
        this.id = id;
        this.name = name;
        this.color = (color != null) ? color : "#000000";
    }

    public Assembly(String name) {
        this.name = name;
    }

    public void setId(Long generatedId) {this.id = generatedId;}
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Color getColor() {return Color.web(color, 1.0);}
    public void setColor(Color color) {this.color = color.toString();}

    public List<Part> getAllParts() {
        return PartRepository.getInstance().getByAssemblyId(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
