package jante.addon;

import jante.model.Addon;
import jante.model.Addon;

/**
 * Requires synchonization between consequetive tests
 */
public interface BetweenTestsAddon extends Addon {
    void beforeNextTest();
}
