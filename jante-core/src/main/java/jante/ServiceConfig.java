package jante;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import jante.model.Addon;
import jante.model.PropertyProvider;
import jante.model.ServiceDefinition;
import jante.util.GuavaHelper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static jante.Injections.injections;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceConfig {
    public final ServiceDefinition serviceDefinition;

    @Wither(AccessLevel.PACKAGE)
    private final ImmutableList<Addon> addons;
    @Wither(AccessLevel.PACKAGE)
    private final ImmutableList<Function<PropertyProvider, Injections>> injectionConfigs;


    public static ServiceConfig serviceConfig(ServiceDefinition serviceDefinition) {
        return new ServiceConfig(serviceDefinition, ImmutableList.of(), ImmutableList.of(props -> injections.bind(props, PropertyProvider.class)));
    }


    Runtime applyProperties(PropertyProvider properties) {
        ImmutableList<Injections> injections = injectionConfigs.stream()
                .map(f -> f.apply(properties))
                .collect(GuavaHelper.listCollector());

        List<Addon> addonsWithProps = addons.stream()
                .map(it -> it.withProperties(properties))
                .collect(toList());

        List<Addon> unFinalizedAddons = sortAddonList(addonsWithProps);
        ImmutableList<Addon> initializedAddons = ImmutableList.of();

        for (Addon addon : unFinalizedAddons) {
            Addon initializedAddon = addon.initialize(new Runtime(serviceDefinition, initializedAddons, injections));
            initializedAddons = GuavaHelper.plus(initializedAddons, initializedAddon);
        }

        List<Injections> modules = initializedAddons.stream()
                .map(Addon::getInjections)
                .collect(toList());

        return new Runtime(serviceDefinition, initializedAddons,
                ImmutableList.<Injections>builder()
                        .addAll(modules)
                        .addAll(injections)
                        .build()
        );
    }


    private static List<Addon> sortAddonList(List<Addon> addons) {
        List<Addon> unSortedList = Lists.newArrayList(addons);
        List<Addon> sortedList = Lists.newArrayList();
        while (unSortedList.size() > 0) {
            List<Addon> addonsWithNoDependencies = unSortedList.stream().filter(possiblyDependent -> {
                Set<Class<?>> dependentOnSet = possiblyDependent.initializeAfter();
                return dependentOnSet.stream().noneMatch(hasDependenciesInList(unSortedList));
            }).collect(Collectors.toList());
            sortedList.addAll(addonsWithNoDependencies);
            unSortedList.removeAll(addonsWithNoDependencies);
            if (addonsWithNoDependencies.isEmpty()) {
                throw new RuntimeException("Dependency loop in addons: " + unSortedList);
            }
        }
        return sortedList;
    }

    private static Predicate<Class<?>> hasDependenciesInList(List<Addon> unSortedList) {
        return dependentOn -> unSortedList.stream().anyMatch(dependentOn::isInstance);
    }


    public ServiceConfig removeAddon(Class<? extends Addon> addon) {
        return this
                .withAddons(
                        ImmutableList.copyOf(addons.stream()
                                .filter(existingAddon -> !addon.isInstance(existingAddon))
                                .collect(toList()))
                );
    }

    public ServiceConfig addon(Addon addon) {
        return withAddons(
                GuavaHelper.plus(addons, addon)
        );
    }

    public ServiceConfig inject(Function<PropertyProvider, Injections> injectionConfig) {
        return withInjectionConfigs(GuavaHelper.plus(this.injectionConfigs, injectionConfig));
    }

    public static class Runtime {
        public final ServiceDefinition serviceDefinition;
        public final AddonRepo addons;
        public final ImmutableList<Injections> injections;

        public Runtime(ServiceDefinition serviceDefinition, Iterable<Addon> addons, ImmutableList<Injections> injections) {
            this.serviceDefinition = serviceDefinition;
            this.addons = new AddonRepo(ImmutableList.copyOf(addons));
            this.injections = injections;
        }


        public List<Injections.Registrator> getRegistrators() {
            return injections.stream()
                    .map(it -> it.registrators.stream())
                    .flatMap(Function.identity())
                    .collect(toList());
        }

        public Iterable<Injections.Binder> getBindings() {
            return injections.stream()
                    .map(it -> it.binders.stream())
                    .flatMap(Function.identity())
                    .collect(toList());
        }
    }

}
