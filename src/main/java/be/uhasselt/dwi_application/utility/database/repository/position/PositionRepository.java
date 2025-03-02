package be.uhasselt.dwi_application.utility.database.repository.position;

import be.uhasselt.dwi_application.model.basic.Position;
import be.uhasselt.dwi_application.model.workInstruction.AssemblyInstruction;
import be.uhasselt.dwi_application.utility.database.Database;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;

import java.util.List;
import java.util.Optional;

public class PositionRepository {
    private final PositionDao dao;
    private static PositionRepository instance;

    private PositionRepository() {
        Jdbi jdbi = Database.getJdbi();
        jdbi.registerRowMapper(BeanMapper.factory(Position.class));
        this.dao = jdbi.onDemand(PositionDao.class);
    }

    public static synchronized PositionRepository getInstance() {
        if (instance == null) {
            instance = new PositionRepository();
        }
        return instance;
    }

    public Long add(Position position) {
        Long GeneratedId = dao.insert(position.getX(), position.getY());
        position.setId(GeneratedId);
        return GeneratedId;
    }

    public Optional<Position> getPositionById(Long id) {
        return dao.findById(id);
    }

    public List<Position> getAllPositions() {
        return dao.findAll();
    }

    public List<Position> getAllByInstructionId(Long id) {
        return dao.findAllByInstructionId(id);
    }

    public void deleteAllPositions() {
        dao.deleteAll();
    }

    public List<Long> addPositionsBatch(List<Position> positions) {
        List<Long> generatedIds = dao.insertBatch(positions);

        for (int i = 0, size = positions.size(); i < size; i++) {
            positions.get(i).setId(generatedIds.get(i));
        }
        return generatedIds;
    }

    public void clearPositionsOfAssemblyInstruction(AssemblyInstruction assemblyInstruction) {
        dao.clearPositionsOfAssemblyInstruction(assemblyInstruction.getId());
    }
}
