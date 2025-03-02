package be.uhasselt.dwi_application.utility.database.repository.position;

import be.uhasselt.dwi_application.model.basic.Position;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.*;

import java.util.List;
import java.util.Optional;

@RegisterBeanMapper(Position.class)
public interface PositionDao {
    @SqlUpdate("INSERT INTO Position (X, Y) VALUES (:x, :y)")
    @GetGeneratedKeys("id")
    Long insert(@Bind("x") double x, @Bind("y") double y);

    @SqlBatch("INSERT INTO Position (X, Y) VALUES (:x, :y)")
    @BatchChunkSize(100)
    @GetGeneratedKeys("id")
    List<Long> insertBatch(@BindBean List<Position> positions);

    @SqlQuery("SELECT * FROM Position WHERE id = :id")
    @RegisterBeanMapper(Position.class)
    Optional<Position> findById(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM Position")
    @RegisterBeanMapper(Position.class)
    List<Position> findAll();

    @SqlUpdate("UPDATE Position SET x = :x, y = :y WHERE id = :id")
    void update(@BindBean Position position);

    @SqlUpdate("DELETE FROM Position WHERE id = :id")
    void delete(@Bind("id") Long id);

    @SqlUpdate("DELETE FROM Position")
    void deleteAll();


    @SqlQuery("SELECT p.* FROM Position p " +
            "JOIN AssemblyInstruction_Position aip ON p.id = aip.position_id " +
            "WHERE aip.instruction_id = :id")
    List<Position> findAllByInstructionId(@Bind("id") Long instructionId);

    @SqlUpdate("DELETE FROM Position WHERE id IN (" +
            "SELECT position_id FROM AssemblyInstruction_Position WHERE instruction_id = :id)")
    void clearPositionsOfAssemblyInstruction(@Bind("id") Long id);
}
