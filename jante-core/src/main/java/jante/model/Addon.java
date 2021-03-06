package jante.model;

import com.google.common.collect.ImmutableSet;
import jante.Injections;
import jante.JettyServer;
import jante.ServiceConfig;

import java.util.Set;

public interface Addon {
    /**
     * Lets addon access application properties. Should return a clone of the addon with new configuration based on properties, or this if no property changes.
     * As a matter of convention, proprties have priority over configuration in code
     * <p>
     * runs before initialize, addToJerseyConfig and addToJettyServer
     */
    default Addon withProperties(PropertyProvider properties) {
        return this;
    }

    /**
     * Lets addon initialize state that will be used in runtime, and pull state from already finalized addons.
     * Should return a clone of the addon with the new state.
     * By convention, an Addon should store its state in a nested class, Runtime, so it is accessible from other addons
     * <p>
     * Runs before addToJerseyConfig and addToJettyServer
     */
    default Addon initialize(ServiceConfig.Runtime config) {
        return this;
    }

    /**
     * Register any injections
     */
    default Injections getInjections() {
        return Injections.injections;
    }

    /**
     * Modifies jetty to incorporate addon.
     */
    default JettyServer addToJettyServer(JettyServer jettyServer) {
        return jettyServer;
    }

    /**
     * Returns a list of addon classes that the addon should be finalized after. Thus this addon
     * may utilize said addons in its initialize step.
     * <p>
     * WARNING: dependency resolution is not transient. Thus if A->B->C, A.initializeAfter should return (B,C) and B.finalizeafter should return (C).
     */
    default Set<Class<?>> initializeAfter() {
        return ImmutableSet.of();
    }

    /**
     * Removes any lingering state upon stopping.
     */
    default void cleanUp() {
    }
}
