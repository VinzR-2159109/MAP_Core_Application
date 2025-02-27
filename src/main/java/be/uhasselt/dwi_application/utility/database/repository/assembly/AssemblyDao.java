package be.uhasselt.dwi_application.utility.database.repository.assembly;

import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

@RegisterConstructorMapper(Assembly.class)
public interface AssemblyDao {

    @SqlQuery("SELECT * FROM assembly WHERE id = :id")
    Assembly findById(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM assembly")
    List<Assembly> findAll();

    @SqlUpdate("INSERT INTO assembly (name) VALUES (:name)")
    @GetGeneratedKeys
    Long insert(@BindBean Assembly assembly);

    @SqlUpdate("UPDATE assembly SET name = :name, color = :color WHERE id = :id")
    void update(@Bind("id") Long id, @Bind("name") String name, @Bind("color") String color);


    @SqlUpdate("DELETE FROM assembly WHERE id = :id")
    void delete(@Bind("id") Long id);
}
