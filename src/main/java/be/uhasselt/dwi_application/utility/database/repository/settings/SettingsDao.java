package be.uhasselt.dwi_application.utility.database.repository.settings;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;

@RegisterBeanMapper(Settings.class)
public interface SettingsDao {

    @SqlQuery("SELECT * FROM settings LIMIT 1")
    Settings getSettings();

    @SqlUpdate("INSERT INTO settings (GRID_SIZE) VALUES (:gridSize)")
    @GetGeneratedKeys
    long insertSettings(@Bind Settings settings);

    @SqlUpdate("UPDATE settings SET GRID_SIZE = :gridSize WHERE id = :id")
    void updateSettings(@BindBean Settings settings);
}
