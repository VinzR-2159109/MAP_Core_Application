package be.uhasselt.dwi_application.model.workInstruction;

import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

//God class for polymorphism
public class InstructionRecord {
    private final Long id;
    private final String description;
    private final String type;
    private final Long assemblyId;
    private final Long parentId;
    private final Long partId;
    private final String imagePath;
    private final String instructionHint;
    private final String properties;
    private final int quantity;

    @JdbiConstructor
    public InstructionRecord(@ColumnName("id") Long id,
                             @ColumnName("description") String description,
                             @ColumnName("type") String type,
                             @ColumnName("parent_instruction_id") Long parentId,
                             @ColumnName("assembly_id") Long assemblyId,
                             @ColumnName("part_id") Long partId,
                             @ColumnName("image_path") String imagePath,
                             @ColumnName("instruction_hint") String instructionHint,
                             @ColumnName("properties") String properties,
                             @ColumnName("part_quantity") int quantity) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.assemblyId = assemblyId;
        this.parentId = parentId;
        this.partId = partId;
        this.imagePath = imagePath;
        this.instructionHint = instructionHint;
        this.properties = properties;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public Long getAssemblyId() { return assemblyId; }
    public Long getParentId() { return parentId; }
    public Long getPartId() { return partId; }
    public String getImagePath() { return imagePath; }
    public String getInstructionHint() { return instructionHint; }
    public String getProperties() { return properties; }
    public int getQuantity() {return quantity;}
}

