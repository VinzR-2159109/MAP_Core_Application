package be.uhasselt.dwi_application.utility.database.repository.part;

import be.uhasselt.dwi_application.model.picking.Part;
import be.uhasselt.dwi_application.model.picking.PickingBin;
import be.uhasselt.dwi_application.utility.database.Database;
import be.uhasselt.dwi_application.utility.database.repository.pickingBin.BinDao;
import org.jdbi.v3.core.Jdbi;

import java.util.Collection;
import java.util.List;

public class PartRepository {
    private final Jdbi jdbi;
    private static PartRepository instance;

    private PartRepository() {
        this.jdbi = Database.getJdbi();
        instance = this;
    }

    public static PartRepository getInstance() {
        if (instance == null) {
            instance = new PartRepository();
        }
        return instance;
    }

    public List<Part> getAll() {
        return jdbi.withExtension(PartDao.class, PartDao::findAll);
    }

    public Part getById(Long id) {
        return jdbi.withExtension(PartDao.class, dao -> dao.findById(id));
    }


    public Long add(Part part) {
        Long generatedId = jdbi.withHandle(handle -> {
            PartDao dao = handle.attach(PartDao.class);
            return dao.insertPart(part);
        });
        part.setId(generatedId);
        return generatedId;
    }


    public void delete(Part part) {
        Long partId = part.getId();
        jdbi.useTransaction(handle -> {
            BinDao binDao = handle.attach(BinDao.class);
            PartDao partDao = handle.attach(PartDao.class);

            // Removing part from bins
            List<PickingBin> binsWithPart = binDao.findBinsByPartId(partId);
            for (PickingBin bin : binsWithPart) {
                bin.removePart();
                binDao.updateBin(bin);
                System.out.println("Removed part " + partId + " from bin " + bin.getId());
            }


            partDao.deletePart(partId);
            System.out.println("Successfully deleted part: " + partId);
        });
    }

    public void deleteAsCollection(Collection<Part> selectedParts){
        for (Part part : selectedParts) {
            delete(part);
        }
    }

    public void update(Part part) {
        jdbi.useTransaction(handle -> {
            PartDao dao = handle.attach(PartDao.class);
            dao.updatePart(part);
        });
    }

    public List<Part> getByAssemblyId(Long assemblyId) {
        return jdbi.withHandle(handle -> {
            PartDao dao = handle.attach(PartDao.class);
            return dao.findPartsByAssemblyId(assemblyId);
        });
    }
}
