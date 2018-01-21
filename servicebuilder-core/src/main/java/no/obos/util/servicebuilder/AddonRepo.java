package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.addon.NamedAddon;
import no.obos.util.servicebuilder.model.Addon;

import java.util.List;

import static java.util.stream.Collectors.toList;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddonRepo {
    @Wither(AccessLevel.PACKAGE)
    final ImmutableList<Addon> addons;

    public final static AddonRepo addonRepo = new AddonRepo(ImmutableList.of());

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

    public boolean isAddonPresent(Class<? extends Addon> swaggerAddonClass) {
        return addonInstance(swaggerAddonClass) != null;
    }
}
