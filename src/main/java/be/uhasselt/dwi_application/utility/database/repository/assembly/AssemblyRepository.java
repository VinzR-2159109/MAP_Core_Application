package be.uhasselt.dwi_application.utility.database.repository.assembly;

import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.utility.database.Database;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class AssemblyRepository {
    private final Jdbi jdbi;
    private static AssemblyRepository instance;

    private AssemblyRepository() {
        this.jdbi = Database.getJdbi();
        instance = this;
    }

    public static AssemblyRepository getInstance() {
        if (instance == null) {
            instance = new AssemblyRepository();
        }
        return instance;
    }

    public List<Assembly> getAll() {
        return jdbi.withHandle( handle -> {
            AssemblyDao assemblyDao = handle.attach(AssemblyDao.class);
            return assemblyDao.findAll();
        });
    }

    public Assembly getById(Long id) {
        return jdbi.withHandle(handle -> {
            AssemblyDao assemblyDao = handle.attach(AssemblyDao.class);
            return assemblyDao.findById(id);
        });
    }

    public Long insert(Assembly assembly) {
        Long generatedId = jdbi.withHandle(handle -> {
            AssemblyDao assemblyDao = handle.attach(AssemblyDao.class);
            return assemblyDao.insert(assembly);
        });
        assembly.setId(generatedId);
        return generatedId;
    }

    public void delete(Assembly assembly) {
        Long id = assembly.getId();
        jdbi.useTransaction(handle -> {
            AssemblyDao assemblyDao = handle.attach(AssemblyDao.class);
            assemblyDao.delete(id);
        });
    }


    public void deleteAsList(List<Assembly> selectedAssemblies) {
        for (Assembly assembly : selectedAssemblies) {
            delete(assembly);
        }
    }

    public void updateAssembly(Assembly assembly) {
        jdbi.useTransaction(handle -> {
            AssemblyDao assemblyDao = handle.attach(AssemblyDao.class);
            assemblyDao.update(assembly);
        });
    }
}
