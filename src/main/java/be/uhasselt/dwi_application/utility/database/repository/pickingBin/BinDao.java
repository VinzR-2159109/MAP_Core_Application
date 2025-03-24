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

    @SqlUpdate("INSERT INTO bins (id, POS_X, POS_Y, part_id) VALUES (:id, :pos_x, :pos_y, :partId)")
    void insertBin(@BindBean PickingBin bin);

    @SqlQuery("SELECT * FROM bins WHERE id = :id")
    PickingBin findById(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM bins")
    List<PickingBin> findAll();

    @SqlUpdate("UPDATE BINS SET POS_X = :pos_x, POS_Y = :pos_y, part_id = :partId WHERE id = :id")
    void updateBin(@BindBean PickingBin bin);

    @SqlUpdate("DELETE FROM BINS WHERE id = :id")
    void deleteBin(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM PART WHERE ID = :id")
    Part findPartByBinId(@Bind("id") Long id);

    @SqlUpdate("UPDATE BINS SET part_id = NULL WHERE part_id = :partId")
    void removePartFromBin(@Bind("partId") Long partId);

    @SqlQuery("SELECT * FROM BINS WHERE part_id = :partId")
    List<PickingBin> findBinsByPartId(@Bind("partId") Long partId);
}
