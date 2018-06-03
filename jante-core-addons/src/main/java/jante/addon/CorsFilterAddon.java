package jante.addon;

import jante.Injections;
import jante.cors.ResponseCorsFilter;
import jante.model.Addon;

import static jante.Injections.injections;

/**
 * Implementerer en mest mulig liberal CORS-protokoll basert på https://mortoray.com/2014/04/09/allowing-unlimited-access-with-cors/ .
 * Verdier gitt i konfiurasjon (origin, methods, headers) er fallbackverdier.
 */
public class CorsFilterAddon implements Addon {
    public static CorsFilterAddon corsFilterAddon = new CorsFilterAddon();

    @Override
    public Injections getInjections() {
        return injections
                .register(ResponseCorsFilter.class);
    }
}
