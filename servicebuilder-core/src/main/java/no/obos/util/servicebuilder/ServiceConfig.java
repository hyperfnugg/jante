package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.PropertyProvider;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.util.GuavaHelper;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static no.obos.util.servicebuilder.CdiModule.cdiModule;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceConfig {
    public final ServiceDefinition serviceDefinition;

    @Wither(AccessLevel.PACKAGE)
    public final AddonRepo addons;
    @Wither(AccessLevel.PACKAGE)
    private final ImmutableList<CdiModule> cdiModules;


    public static ServiceConfig serviceConfig(ServiceDefinition serviceDefinition) {
        return new ServiceConfig(serviceDefinition, AddonRepo.addonRepo, ImmutableList.of());
    }


    ServiceConfig applyProperties(PropertyProvider properties) {
        ServiceConfig withProps = this
                .cdiModule(cdiModule
                        .bind(properties, PropertyProvider.class)
                )
                .withAddons(addons.withAddons(
                        ImmutableList.copyOf(this
                                .addons.addons.stream()
                                .map(it -> it.withProperties(properties))
                                .collect(toList()
                                ))
                        )
                );

        List<Addon> unFinalizedAddons = sortAddonList(withProps.addons.addons);
        ServiceConfig withFinalizedAddons = withProps.withAddons(AddonRepo.addonRepo);
        for (Addon addon : unFinalizedAddons) {
            withFinalizedAddons = withFinalizedAddons.addon(addon.initialize(withFinalizedAddons));
        }

        List<CdiModule> modules = withFinalizedAddons.addons.addons.stream()
                .map(Addon::getCdiModule)
                .collect(toList());
        return withFinalizedAddons.withCdiModules(
                ImmutableList.<CdiModule>builder()
                        .addAll(modules)
                        .addAll(withFinalizedAddons.cdiModules)
                        .build()
        );
    }

    public List<JerseyConfig.Registrator> getRegistrators() {
        return cdiModules.stream()
                .map(it -> it.registrators.stream())
                .flatMap(Function.identity())
                .collect(toList());
    }

    public Iterable<JerseyConfig.Binder> getBindings() {
        return cdiModules.stream()
                .map(it -> it.binders.stream())
                .flatMap(Function.identity())
                .collect(toList());
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
                .withAddons(addons.withAddons(
                        ImmutableList.copyOf(addons.addons.stream()
                                .filter(existingAddon -> !addon.isInstance(existingAddon))
                                .collect(toList()))
                        )
                );
    }

    public ServiceConfig addon(Addon addon) {
        return withAddons(addons.withAddons(
                GuavaHelper.plus(addons.addons, addon))
        );
    }

    public ServiceConfig cdiModule(CdiModule cdiModule) {
        return withCdiModules(GuavaHelper.plus(this.cdiModules, cdiModule));
    }

    @AllArgsConstructor
    public static class Runtime {
        public final ServiceDefinition serviceDefinition;
        public final AddonRepo addons;
        private final ImmutableList<CdiModule> cdiModules;
    }

}
