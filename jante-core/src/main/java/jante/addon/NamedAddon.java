package jante.addon;

import jante.model.Addon;
import jante.model.Addon;

/**
 * Addon allows several instances, using name to distinguish (e.g. several database connections)
 */
public interface NamedAddon extends Addon {
    String getName();
}
