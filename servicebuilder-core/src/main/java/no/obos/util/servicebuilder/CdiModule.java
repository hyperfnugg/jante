package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.model.PropertyProvider;
import no.obos.util.servicebuilder.util.GuavaHelper;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Singleton;
import java.util.function.BiConsumer;
import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CdiModule {
    @Wither(AccessLevel.PRIVATE)
    public final ImmutableList<JerseyConfig.Binder> binders;
    @Wither(AccessLevel.PRIVATE)
    public final ImmutableList<JerseyConfig.Registrator> registrators;
    @Wither(AccessLevel.PRIVATE)
    public final ImmutableList<Function<PropertyProvider, JerseyConfig.Hk2ConfigModule>> hk2ConfigProp;

    public final static CdiModule cdiModule = new CdiModule(ImmutableList.of(), ImmutableList.of(), ImmutableList.of());

    public <T> CdiModule bind(Class<? extends T> toBind, Class<T> bindTo) {
        return bind(binder -> binder.bind(toBind).to(bindTo));
    }

    public <T> CdiModule bind(T toBind, Class<? super T> bindTo) {
        return bind(binder -> binder.bind(toBind).to(bindTo));
    }

    public <T> CdiModule bind(Class<T> toBind) {
        return bind(binder -> binder.bindAsContract(toBind));
    }


    public <T> CdiModule bindSingleton(Class<T> toBind, Class<? super T> bindTo) {
        return bind(binder -> binder.bind(toBind).to(bindTo).in(Singleton.class));
    }

    public <T> CdiModule bindNamed(Class<? extends T> toBind, Class<T> bindTo, String name) {
        return bind(binder -> binder.bind(toBind).to(bindTo).named(name));
    }

    public <T> CdiModule bindNamed(T toBind, Class<? super T> bindTo, String name) {
        return bind(binder -> binder.bind(toBind).to(bindTo).named(name));
    }


    public <T> CdiModule bindNamed(Class<T> toBind, String name) {
        return bind(binder -> binder.bindAsContract(toBind).named(name));
    }

    public <T, Y extends Factory<T>> CdiModule bindFactory(Y toBind, Class<T> bindTo) {
        return bind(binder -> binder.bindFactory(toBind).to(bindTo));
    }

    public <T> CdiModule bindFactory(Class<? extends Factory<T>> toBind, Class<T> bindTo) {
        return bind(binder -> binder.bindFactory(toBind).to(bindTo));
    }

    public CdiModule bind(JerseyConfig.Binder binder) {
        return withBinders(GuavaHelper.plus(binders, binder));
    }

    public CdiModule bindWithProps(BiConsumer<PropertyProvider, AbstractBinder> propertyBinder) {
        return hk2ConfigModule(props ->
                new JerseyConfig.Hk2ConfigModule() {
                    @Override
                    public void addBindings(AbstractBinder binder) {
                        propertyBinder.accept(props, binder);

                    }

                    @Override
                    public void applyRegistations(ResourceConfig resourceConfig) {
                    }
                }
        );
    }

    public CdiModule registerInstance(Object toRegister) {
        return register(registrator -> registrator.register(toRegister));
    }

    public CdiModule register(Class toRegister) {
        return register(registrator -> registrator.register(toRegister));
    }


    public CdiModule register(JerseyConfig.Registrator registrator) {
        return withRegistrators(GuavaHelper.plus(registrators, registrator));
    }

    public CdiModule hk2ConfigModule(JerseyConfig.Hk2ConfigModule hk2ConfigModule) {
        return register(hk2ConfigModule)
                .bind(hk2ConfigModule);
    }

    public CdiModule hk2ConfigModule(Function<PropertyProvider, JerseyConfig.Hk2ConfigModule> prop2Hk2) {
        return withHk2ConfigProp(GuavaHelper.plus(hk2ConfigProp, prop2Hk2));
    }

    CdiModule addPropertiesAndApplyToBindings(PropertyProvider properties) {
        CdiModule ret = this.withHk2ConfigProp(ImmutableList.of());
        for (Function<PropertyProvider, JerseyConfig.Hk2ConfigModule> i : hk2ConfigProp) {
            ret = ret.hk2ConfigModule(i.apply(properties));
        }
        return ret;

    }
}
