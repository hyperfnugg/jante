package jante;

import com.google.common.collect.ImmutableList;
import jante.util.GuavaHelper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Singleton;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Injections {
    @Wither(AccessLevel.PRIVATE)
    public final ImmutableList<Binder> binders;
    @Wither(AccessLevel.PRIVATE)
    public final ImmutableList<Registrator> registrators;

    public final static Injections injections = new Injections(ImmutableList.of(), ImmutableList.of());

    public <T> Injections bind(Class<? extends T> toBind, Class<T> bindTo) {
        return bind(binder -> binder.bind(toBind).to(bindTo));
    }

    public <T> Injections bind(T toBind, Class<? super T> bindTo) {
        return bind(binder -> binder.bind(toBind).to(bindTo));
    }

    public <T> Injections bind(Class<T> toBind) {
        return bind(binder -> binder.bindAsContract(toBind));
    }


    public <T> Injections bindSingleton(Class<T> toBind, Class<? super T> bindTo) {
        return bind(binder -> binder.bind(toBind).to(bindTo).in(Singleton.class));
    }

    public <T> Injections bindNamed(Class<? extends T> toBind, Class<T> bindTo, String name) {
        return bind(binder -> binder.bind(toBind).to(bindTo).named(name));
    }

    public <T> Injections bindNamed(T toBind, Class<? super T> bindTo, String name) {
        return bind(binder -> binder.bind(toBind).to(bindTo).named(name));
    }


    public <T> Injections bindNamed(Class<T> toBind, String name) {
        return bind(binder -> binder.bindAsContract(toBind).named(name));
    }

    public <T, Y extends Factory<T>> Injections bindFactory(Y toBind, Class<T> bindTo) {
        return bind(binder -> binder.bindFactory(toBind).to(bindTo));
    }

    public <T> Injections bindFactory(Class<? extends Factory<T>> toBind, Class<T> bindTo) {
        return bind(binder -> binder.bindFactory(toBind).to(bindTo));
    }

    public Injections bind(Binder binder) {
        return withBinders(GuavaHelper.plus(binders, binder));
    }

    public Injections registerInstance(Object toRegister) {
        return register(registrator -> registrator.register(toRegister));
    }

    public Injections register(Class toRegister) {
        return register(registrator -> registrator.register(toRegister));
    }


    public Injections register(Registrator registrator) {
        return withRegistrators(GuavaHelper.plus(registrators, registrator));
    }

    public interface Binder {
        void addBindings(AbstractBinder binder);
    }

    public interface Registrator {
        void applyRegistations(ResourceConfig resourceConfig);
    }
}
