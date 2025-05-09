package be.uhasselt.dwi_application.utility.database.repository.part;

import be.uhasselt.dwi_application.model.workInstruction.picking.Part;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.*;

import java.util.List;

@RegisterBeanMapper(Part.class)
public interface PartDao {
    @SqlQuery("SELECT * FROM Part WHERE id = :id")
    Part findById(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM Part")
    List<Part> findAll();

    @SqlUpdate("DELETE FROM Part WHERE id = :id")
    void deletePart(@Bind("id") Long id);

    @SqlUpdate("UPDATE Part SET name = :name, assembly_id = :assemblyId, BIN_ID = :binId WHERE id = :id")
    void updatePart(@BindBean Part part);

    @SqlUpdate("INSERT INTO Part (name, assembly_id) VALUES (:name, :assemblyId)")
    @GetGeneratedKeys
    Long insertPart(@BindBean Part part);

    @SqlQuery("SELECT * FROM part WHERE assembly_id = :assemblyId")
    List<Part> findPartsByAssemblyId(@Bind("assemblyId") Long assemblyId);

    @SqlUpdate("DELETE FROM part WHERE assembly_id = :assemblyId")
    void deletePartsByAssemblyId(@Bind("assemblyId") Long assemblyId);
}
