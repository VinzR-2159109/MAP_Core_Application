package be.uhasselt.dwi_application.utility.database.repository.pickingBin;

import be.uhasselt.dwi_application.model.workInstruction.picking.Part;
import be.uhasselt.dwi_application.model.workInstruction.picking.PickingBin;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;

import java.util.List;

@RegisterBeanMapper(PickingBin.class)
public interface BinDao {

    @SqlUpdate("INSERT INTO bins VALUES (:bin)")
    void insertBin(@BindBean PickingBin bin);

    @SqlQuery("SELECT * FROM bins WHERE id = :id")
    PickingBin findById(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM bins")
    List<PickingBin> findAll();

    @SqlUpdate("DELETE FROM BINS WHERE id = :id")
    void deleteBin(@Bind("id") Long id);
}
