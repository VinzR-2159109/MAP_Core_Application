package be.uhasselt.dwi_application.utility.database.repository.instruction;

import be.uhasselt.dwi_application.model.workInstruction.AssemblyInstruction;
import be.uhasselt.dwi_application.model.workInstruction.Instruction;
import be.uhasselt.dwi_application.model.workInstruction.InstructionRecord;
import be.uhasselt.dwi_application.model.workInstruction.PickingInstruction;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import java.util.List;

@RegisterConstructorMapper(Instruction.class)
@RegisterConstructorMapper(InstructionRecord.class)
public interface InstructionDao {

    // Fetch a single instruction by ID
    @SqlQuery("SELECT * FROM instruction WHERE id = :id")
    Instruction findById(@Bind("id") Long id);

    // Fetch all instructions (both Assembly & Picking)
    @SqlQuery("SELECT * FROM instruction")
    List<Instruction> findAll();

    // Fetch sub-instructions for a given AssemblyInstruction
    @SqlQuery("SELECT * FROM instruction WHERE parent_instruction_id = :parentId")
    List<Instruction> findSubInstructions(@Bind("parentId") Long parentId);

    // Insert a new AssemblyInstruction
    @SqlUpdate("INSERT INTO instruction (description, type, assembly_id, parent_instruction_id) VALUES (:description, 'ASSEMBLY', :assemblyId, :parentId)")
    @GetGeneratedKeys
    Long insertAssemblyInstruction(@Bind("description") String description, @Bind("assembly_id") Long assemblyId, @Bind("parent_instruction_id") Long parentId);

    // Insert a new PickingInstruction
    @SqlUpdate("INSERT INTO instruction (description, type, assembly_id, parent_instruction_id, part_id) VALUES (:description, 'PICKING', :assemblyId, :parentId, :partId)")
    @GetGeneratedKeys
    Long insertPickingInstruction(@Bind("description") String description, @Bind("assembly_id") Long assemblyId, @Bind("parent_instruction_id") Long parentId, @Bind("part_id") Long partId);

    // Update:
    @SqlUpdate("UPDATE INSTRUCTION SET description = :description WHERE id = :id")
    void update(@BindBean Instruction instruction);

    @SqlUpdate("UPDATE INSTRUCTION SET description = :description, part_id = :partId, INSTRUCTION_HINT = :hint, image_path = :imagePath, properties = :properties, PART_QUANTITY = :quantity WHERE id = :id")
    void updatePickingInstruction(@Bind("id") Long id, @Bind("description") String description, @Bind("partId") Long partId, @Bind("hint") String hint, @Bind("imagePath") String imagePath, @Bind("properties") String properties, @Bind("quantity") int quantity);

    @SqlUpdate("UPDATE INSTRUCTION SET description = :description, INSTRUCTION_HINT = :hint, image_path = :imagePath, properties = :properties WHERE id = :id")
    void updateAssemblyInstruction(@Bind("id") Long id, @Bind("description") String description, @Bind("hint") String hint, @Bind("imagePath") String imagePath, @Bind("properties") String properties);

    // Delete
    @SqlUpdate("DELETE FROM instruction WHERE id = :id")
    void delete(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM instruction WHERE assembly_id = :assemblyId")
    List<InstructionRecord> findByAssemblyId(@Bind("assemblyId") Long assemblyId);

    @SqlUpdate("INSERT INTO instruction (description, type, assembly_id, parent_instruction_id, part_id, image_path, properties, instruction_hint, PART_QUANTITY) VALUES (:description, :type, :assemblyId, :parentId, :partId, :imagePath, :properties, :hint, :quantity)")
    @GetGeneratedKeys
    Long insertInstruction(@Bind("description") String description,
                           @Bind("type") String type,
                           @Bind("assemblyId") Long assemblyId,
                           @Bind("parentId") Long parentId,
                           @Bind("partId") Long partId,
                           @Bind("imagePath") String imagePath,
                           @Bind("properties") String properties,
                           @Bind("hint") String hint,
                           @Bind("quantity") int quantity);

    @SqlUpdate("UPDATE instruction SET image_path = :image_path WHERE id = :id")
    void insertImagePath(@Bind("image_path") String imagePath, @Bind("id") Long id);

    @SqlQuery("SELECT image_path FROM instruction WHERE id = :id")
    String findImageByInstructionId(@Bind("id") Long id);
}
