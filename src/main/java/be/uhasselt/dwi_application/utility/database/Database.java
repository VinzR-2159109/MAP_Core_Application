package be.uhasselt.dwi_application.utility.database;

import be.uhasselt.dwi_application.model.basic.Position;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

public class Database {
    private static final Jdbi jdbi = Jdbi.create("jdbc:h2:./data/db", "sa" , "").installPlugin(new SqlObjectPlugin());

    public static Jdbi getJdbi() {
        return jdbi;
    }
}
