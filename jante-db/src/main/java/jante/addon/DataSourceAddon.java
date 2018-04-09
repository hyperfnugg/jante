package jante.addon;

import javax.sql.DataSource;

public interface DataSourceAddon extends NamedAddon {
    String getName();

    DataSource getDataSource();
}
