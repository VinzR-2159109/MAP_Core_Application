package be.uhasselt.dwi_application.utility.database.repository.settings;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;

@RegisterBeanMapper(Settings.class)
public interface SettingsDao {

    @SqlQuery("SELECT * FROM settings LIMIT 1")
    Settings getSettings();

    @SqlUpdate("INSERT INTO settings (GRID_SIZE, enabled_assistance_systems) VALUES (:gridSize, '')")
    @GetGeneratedKeys
    long insertSettings(@Bind Settings settings);

    @SqlUpdate("UPDATE settings SET grid_size =:gridSize, enabled_assistance_systems =:enabledAssistanceSystems, NECESSARY_QOW =:necesarryQOW WHERE id = :id")
    void updateSettings(@Bind("id") Long id,
                        @Bind("gridSize") int gridSize,
                        @Bind("enabledAssistanceSystems") String enabledAssistanceSystems,
                        @Bind("necesarryQOW") int necessaryQOW);

}
