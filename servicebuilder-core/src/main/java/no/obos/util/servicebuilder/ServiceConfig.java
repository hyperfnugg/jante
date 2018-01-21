package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.PropertyProvider;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.util.GuavaHelper;

import java.util.List;
import java.util.function.Function;

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
        return this
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

    public ServiceConfig finishConfig() {
        List<CdiModule> modules = addons.addons.stream()
                .map(Addon::getCdiModule)
                .collect(toList());
        return withCdiModules(
                ImmutableList.<CdiModule>builder()
                        .addAll(modules)
                        .addAll(this.cdiModules)
                        .build()
        );
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

}
