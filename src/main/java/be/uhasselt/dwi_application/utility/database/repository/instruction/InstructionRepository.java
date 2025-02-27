package be.uhasselt.dwi_application.utility.database.repository.instruction;

import be.uhasselt.dwi_application.model.picking.Part;
import be.uhasselt.dwi_application.model.workInstruction.*;
import be.uhasselt.dwi_application.utility.database.Database;
import be.uhasselt.dwi_application.utility.database.repository.part.PartDao;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.customizer.Bind;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class InstructionRepository {

    private final Jdbi jdbi;
    private static InstructionRepository instance;

    private InstructionRepository() {
        this.jdbi = Database.getJdbi();
        instance = this;
    }

    public static InstructionRepository getInstance() {
        if (instance == null) {
            instance = new InstructionRepository();
        }
        return instance;
    }

    public void insertAssemblyInstruction(AssemblyInstruction assemblyInstruction) {
        String ASSEMBLY = assemblyInstruction.getType();
        if (!Objects.equals(ASSEMBLY, "ASSEMBLY")) {
            throw new IllegalArgumentException("Type of assembly instruction must be ASSEMBLY");
        }

        Long generatedId =  jdbi.withHandle(_ -> insertInstruction(assemblyInstruction.getDescription(), ASSEMBLY, assemblyInstruction.getAssemblyId(), assemblyInstruction.getParentInstructionId(), null, assemblyInstruction.getImagePath(), assemblyInstruction.getPropertiesAsString(), assemblyInstruction.getHint(), 0));
        assemblyInstruction.setId(generatedId);
    }

    public void insertPickingInstruction(PickingInstruction pickingInstruction) {
        String PICKING = pickingInstruction.getType();
        if (!Objects.equals(PICKING, "PICKING")) {
            throw new IllegalArgumentException("Type of assembly instruction must be PICKING");
        }

        Long generatedId =  jdbi.withHandle(_ -> insertInstruction(pickingInstruction.getDescription(), PICKING, pickingInstruction.getAssemblyId(), pickingInstruction.getParentInstructionId(), pickingInstruction.getPartId(), pickingInstruction.getImagePath(), pickingInstruction.getPropertiesAsString(), pickingInstruction.getHint(), pickingInstruction.getQuantity()));
        pickingInstruction.setId(generatedId);
    }

    private Long insertInstruction(String description, String type, Long assemblyId, Long parentId, Long partId, String imagePath, String properties, String hint, int quantity) {
        return jdbi.withHandle(handle -> {
            InstructionDao dao = handle.attach(InstructionDao.class);
            return dao.insertInstruction(description, type, assemblyId, parentId, partId, imagePath, properties, hint, quantity);
        });
    }


    public void updateInstruction(Instruction instruction) {
        jdbi.useTransaction(handle -> {
            InstructionDao dao = handle.attach(InstructionDao.class);

            if (instruction instanceof PickingInstruction pickingInstruction) {
                dao.updatePickingInstruction(pickingInstruction.getId(), pickingInstruction.getDescription(), pickingInstruction.getPartId(), pickingInstruction.getHint(), pickingInstruction.getImagePath(), pickingInstruction.getPropertiesAsString(), pickingInstruction.getQuantity());
            } else if (instruction instanceof AssemblyInstruction assemblyInstruction) {
                dao.updateAssemblyInstruction(assemblyInstruction.getId(), assemblyInstruction.getDescription(), assemblyInstruction.getHint(), assemblyInstruction.getImagePath(), assemblyInstruction.getPropertiesAsString());
            } else {
                dao.update(instruction);
            }
        });

    }


    /**
     * Delete an instruction (removes sub-instructions automatically due to ON DELETE CASCADE).
     */
    public void deleteInstruction(Long id) {
        jdbi.useTransaction(handle -> {
            InstructionDao dao = handle.attach(InstructionDao.class);
            dao.delete(id);
        });
    }

    public List<Instruction> findByAssembly(Assembly assembly) {
        Long assemblyId = assembly.getId();
        return jdbi.withHandle(handle -> {
            InstructionDao dao = handle.attach(InstructionDao.class);
            List<InstructionRecord> records = dao.findByAssemblyId(assemblyId);
            return records.stream()
                    .map(record -> {
                        if ("ASSEMBLY".equals(record.getType())) {
                            AssemblyInstruction assemblyInstruction = new AssemblyInstruction(
                                record.getDescription(), record.getAssemblyId(), record.getParentId(), record.getImagePath(), record.getInstructionHint(), record.getProperties()
                            );
                            assemblyInstruction.setId(record.getId());
                            return assemblyInstruction;
                        } else if ("PICKING".equals(record.getType())) {
                            PartDao partDao = handle.attach(PartDao.class);
                            Part part = (record.getPartId() != null) ? partDao.findById(record.getPartId()) : null;

                            PickingInstruction pickingInstruction = new PickingInstruction(
                                record.getDescription(), part, record.getAssemblyId(), record.getParentId(), record.getImagePath(), record.getInstructionHint(), record.getProperties(), record.getQuantity()
                            );
                            pickingInstruction.setId(record.getId());
                            return pickingInstruction;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new));
        });
    }


    public void removeInstruction(Long id) {
        jdbi.useTransaction(handle -> {
            InstructionDao dao = handle.attach(InstructionDao.class);
            dao.delete(id);
        });
    }

    public void uploadImageToInstruction(String imagePath, Long id){
        jdbi.useTransaction(handle -> {
            InstructionDao dao = handle.attach(InstructionDao.class);
            dao.insertImagePath(imagePath, id);
        });
    }

    public String getInstructionImagePath(Long id) {
        return jdbi.withHandle(handle -> {
            InstructionDao dao = handle.attach(InstructionDao.class);
            return dao.findImageByInstructionId(id);
        });
    }
}
