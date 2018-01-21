package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.addon.NamedAddon;
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
    @Wither(AccessLevel.PACKAGE)
    final ImmutableList<Addon> addons;
    public final ServiceDefinition serviceDefinition;
    @Wither(AccessLevel.PACKAGE)
    private final ImmutableList<CdiModule> cdiModules;


    public static ServiceConfig serviceConfig(ServiceDefinition serviceDefinition) {
        return new ServiceConfig(ImmutableList.of(), serviceDefinition, ImmutableList.of());
    }


    ServiceConfig applyProperties(PropertyProvider properties) {
        return this
                .cdiModule(cdiModule
                        .bind(properties, PropertyProvider.class)
                )
                .withAddons(ImmutableList.copyOf(this
                        .addons.stream()
                        .map(it -> it.withProperties(properties))
                        .collect(toList()
                        ))
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

    @SuppressWarnings("unchecked")
    public <T extends Addon> List<T> addonInstances(Class<T> clazz) {
        return (List<T>) this.addons.stream()
                .filter(clazz::isInstance)
                .collect(toList());
    }

    public <T extends Addon> List<T> requireAddonInstanceAtLeastOne(Class<T> clazz) {
        List<T> addons = addonInstances(clazz);
        if (addons.isEmpty()) {
            throw new RuntimeException("Required addon not found. Check config or priorities. " + clazz.getCanonicalName());
        }
        return addons;
    }

    public <T extends Addon> T addonInstance(Class<T> clazz) {
        List<T> ret = addonInstances(clazz);
        if (ret.isEmpty()) {
            return null;
        }
        if (ret.size() > 1) {
            throw new RuntimeException("Found several implementations for addon " + clazz.getCanonicalName());
        }
        return ret.get(0);
    }

    public <T extends NamedAddon> T addonInstanceNamed(Class<T> clazz, String name) {
        List<T> ret = addonInstances(clazz);
        if (name != null) {
            ret = ret.stream().filter(it -> name.equals(it.getName())).collect(toList());
        } else {
            ret = ret.stream().filter(it -> it.getName() == null).collect(toList());
        }

        if (ret.isEmpty()) {
            return null;
        }
        if (ret.size() > 1) {
            throw new RuntimeException("Found several implementations for addon " + clazz.getCanonicalName());
        }
        return ret.get(0);
    }

    public <T extends Addon> T requireAddonInstance(Class<T> clazz) {
        List<T> ret = requireAddonInstanceAtLeastOne(clazz);
        if (ret.size() > 1) {
            throw new RuntimeException("Found several implementations for addon " + clazz.getCanonicalName());
        }
        return ret.get(0);
    }

    public ServiceConfig removeAddon(Class<? extends Addon> addon) {
        return this
                .withAddons(ImmutableList.copyOf(addons.stream()
                        .filter(existingAddon -> !addon.isInstance(existingAddon))
                        .collect(toList()))
                );
    }

    public ServiceConfig clearAddons() {
        return this.withAddons(ImmutableList.of());
    }

    public ServiceConfig addon(Addon addon) {
        return withAddons(GuavaHelper.plus(addons, addon));
    }

    public ServiceConfig cdiModule(CdiModule cdiModule) {
        return withCdiModules(GuavaHelper.plus(this.cdiModules, cdiModule));
    }

    public boolean isAddonPresent(Class<? extends Addon> swaggerAddonClass) {
        return addonInstance(swaggerAddonClass) != null;
    }
}
