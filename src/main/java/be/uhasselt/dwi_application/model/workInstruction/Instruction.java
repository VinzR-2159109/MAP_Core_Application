package be.uhasselt.dwi_application.model.workInstruction;


import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class Instruction {
    private Long id;
    private Long parentInstructionId;
    private Long assemblyId;
    private String description;
    private String type; // 'ASSEMBLY' or 'PICKING'
    private String imagePath;
    private String hint;

    protected Set<InstructionProperty> properties;

    public enum InstructionProperty {
        SKIP_DURING_PLAY,
        HINT_DISABLED
    }

    public Instruction(String description, String type, Long parentInstructionId, Long assemblyId, String imagePath, String hint, String properties) {
        this.description = description;
        this.type = type;
        this.parentInstructionId = parentInstructionId;
        this.assemblyId = assemblyId;
        this.imagePath = imagePath;
        this.hint = hint;

        this.properties = new HashSet<>();
        if (properties != null && !properties.isEmpty()) {
            for (String prop : properties.split(",")) {
                this.properties.add(InstructionProperty.valueOf(prop.trim()));
            }
        }
    }

    public Long getId() { return id; }
    public void setId(Long generatedId) {this.id = generatedId;}

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getAssemblyId() { return assemblyId; }
    public void setAssemblyId(Long assemblyId) { this.assemblyId = assemblyId; }

    public Long getParentInstructionId() {return parentInstructionId;}
    public void setParentInstructionId(Long parentInstructionId) {
        this.parentInstructionId = parentInstructionId;
    }

    public String getImagePath() {return imagePath;}
    public void setImagePath(String imagePath) {this.imagePath = imagePath;}

    public void addProperty(InstructionProperty property) {properties.add(property);}
    public boolean hasProperty(InstructionProperty property) {return properties.contains(property);}

    public void removeProperty(InstructionProperty property) {properties.remove(property);}
    public void removeProperties(List<InstructionProperty> propertiesToRemove) {
        propertiesToRemove.forEach(properties::remove);
    }

    public String getPropertiesAsString() {
        return properties.stream()
                .map(Enum::name)
                .reduce((p1, p2) -> p1 + "," + p2)
                .orElse("");
    }
    public void setProperties(String properties) {
        if (properties != null && !properties.isEmpty()) {
            for (String prop : properties.split(",")) {
                this.properties.add(InstructionProperty.valueOf(prop.trim()));
            }
        }
    }

    public String getHint() {return hint;}
    public void setHint(String hint) {this.hint = hint;}

    @Override
    public String toString() {
        return description;
    }
}
