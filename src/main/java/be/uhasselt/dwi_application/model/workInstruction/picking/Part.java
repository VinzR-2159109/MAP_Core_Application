package be.uhasselt.dwi_application.model.workInstruction.picking;

import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.utility.database.repository.assembly.AssemblyRepository;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class Part {
    @ColumnName("id")
    private Long id;

    @ColumnName("name")
    private String name;

    @ColumnName("assembly_id")
    private Long assemblyId;

    private Assembly assembly; // Lazily loaded when needed

    public Part(String name, Assembly assembly) {
        this.name = name;
        this.assembly = assembly;
        this.assemblyId = assembly.getId();
    }

    public Part(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Part(){}

    @JdbiConstructor
    public Part(@ColumnName("id") Long id, @ColumnName("name") String name, @ColumnName("assembly_id") Long assemblyId) {
        this.id = id;
        this.name = name;
        this.assemblyId = assemblyId;
        this.assembly = null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getAssemblyId() { return assemblyId; }

    public void setAssemblyId(Long assemblyId) {
        this.assemblyId = assemblyId;
        this.assembly = null;
    }

    public Assembly getAssembly() {
        // Lazily load Assembly if it's null
        if (assembly == null) {
            assembly = AssemblyRepository.getInstance().getById(assemblyId);
        }
        return assembly;
    }

    public void setAssembly(Assembly assembly) {
        this.assembly = assembly;
        this.assemblyId = assembly.getId();
    }

    @Override
    public String toString() {
        return name;
    }
}


