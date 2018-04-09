package jante.addon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import jante.CdiModule;
import jante.ServiceConfig;
import jante.exception.DependenceException;
import jante.model.Addon;
import jante.util.GuavaHelper;
import org.glassfish.hk2.api.Factory;
import org.skife.jdbi.v2.DBI;

import javax.sql.DataSource;
import java.util.Set;

import static jante.CdiModule.cdiModule;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbiAddon implements NamedAddon {

    @Getter
    @Wither(AccessLevel.PRIVATE)
    public final String name;
    @Wither(AccessLevel.PRIVATE)
    public final DBI dbi;
    @Wither(AccessLevel.PRIVATE)
    public final ImmutableList<Class<?>> daos;

    public static final JdbiAddon jdbiAddon =
            new JdbiAddon(null, null, ImmutableList.of());

    @Override
    public Addon initialize(ServiceConfig.Runtime config) {
        DataSourceAddon dataSourceAddon = config.addons.addonInstanceNamed(DataSourceAddon.class, name);
        if (dataSourceAddon == null) {
            if (name == null) {
                throw new DependenceException(this.getClass(), DataSourceAddon.class, " no unnamed datasourceaddon found");
            } else {
                throw new DependenceException(this.getClass(), DataSourceAddon.class, " no datasourceaddon for name " + name);
            }
        }
        DataSource dataSource = dataSourceAddon.getDataSource();
        DBI dbi = new DBI(dataSource);
        return this.dbi(dbi);
    }

    @Override
    public CdiModule getCdiModule() {
        CdiModule ret = cdiModule;

        if (name != null) {
            ret = ret
                    .bindNamed(dbi, DBI.class, name)
                    .bindNamed(this, JdbiAddon.class, name);
        } else {
            ret = ret
                    .bind(dbi, DBI.class)
                    .bind(this, JdbiAddon.class);
        }

        for (Class clazz : daos) {
            //noinspection unchecked
            ret = ret.bindFactory(new DaoFactory(dbi, clazz), clazz);
        }
        return ret;
    }

    @AllArgsConstructor
    public static class DaoFactory implements Factory<Object> {

        final DBI dbi;
        final Class<?> clazz;

        public Object provide() {
            return dbi.onDemand(clazz);
        }

        @Override
        public void dispose(Object instance) {

        }
    }

    public <T> T createDao(Class<T> requiredType) {
        return dbi.onDemand(requiredType);
    }


    @Override
    public Set<Class<?>> initializeAfter() {
        return ImmutableSet.of(DataSourceAddon.class);
    }

    public JdbiAddon dao(Class<?> dao) {
        return withDaos(GuavaHelper.plus(daos, dao));
    }

    public JdbiAddon name(String name) {
        return withName(name);
    }

    public JdbiAddon dbi(DBI dbi) {
        return withDbi(dbi);
    }
}
