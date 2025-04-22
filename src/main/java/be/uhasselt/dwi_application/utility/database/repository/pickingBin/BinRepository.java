package be.uhasselt.dwi_application.utility.database.repository.pickingBin;

import be.uhasselt.dwi_application.model.workInstruction.picking.Part;
import be.uhasselt.dwi_application.model.workInstruction.picking.PickingBin;
import be.uhasselt.dwi_application.utility.database.Database;
import be.uhasselt.dwi_application.utility.database.repository.part.PartDao;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class BinRepository {
    private final Jdbi jdbi;
    private static BinRepository instance;

    public BinRepository() {
        this.jdbi = Database.getJdbi();
        instance = this;
    }

    public static BinRepository getInstance() {
        if (instance == null) {
            instance = new BinRepository();
        } else {
            return instance;
        }
        return instance;
    }

    public List<PickingBin> getAll() {
        return jdbi.withExtension(BinDao.class, BinDao::findAll);
    }

    public PickingBin getById(Long id) {
        return jdbi.withHandle(handle -> {
            BinDao dao = handle.attach(BinDao.class);
            return dao.findById(id);
        });
    }

    public void add(PickingBin bin) {
        jdbi.useTransaction(handle -> {
            BinDao dao = handle.attach(BinDao.class);
            dao.insertBin(bin);
        });
    }

    public void delete(PickingBin bin) {
        Long id = bin.getId();
        jdbi.useTransaction(handle -> {
            BinDao dao = handle.attach(BinDao.class);
            dao.deleteBin(id);
        });
    }

    public void deleteAsList(List<PickingBin> selectedBins) {
        for (PickingBin bin : selectedBins) {
            delete(bin);
        }
    }
}
