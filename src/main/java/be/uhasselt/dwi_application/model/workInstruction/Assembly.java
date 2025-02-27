package be.uhasselt.dwi_application.model.workInstruction;

import be.uhasselt.dwi_application.model.picking.Part;
import be.uhasselt.dwi_application.utility.database.repository.part.PartRepository;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Assembly {
    private Long id;
    private String name;

    @JdbiConstructor
    public Assembly(@ColumnName("id") Long id, @ColumnName("name") String name) {
        this.id = id;
        this.name = name;
    }

    public Assembly(String name) {
        this.name = name;
    }

    public void setId(Long generatedId) {this.id = generatedId;}
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Part> getAllParts() {
        return PartRepository.getInstance().getByAssemblyId(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
