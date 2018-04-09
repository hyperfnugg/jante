package jante.addon;

import jante.CdiModule;
import jante.cors.ResponseCorsFilter;
import jante.model.Addon;

import static jante.CdiModule.cdiModule;

/**
 * Implementerer en mest mulig liberal CORS-protokoll basert på https://mortoray.com/2014/04/09/allowing-unlimited-access-with-cors/ .
 * Verdier gitt i konfiurasjon (origin, methods, headers) er fallbackverdier.
 */
public class CorsFilterAddon implements Addon {
    public static CorsFilterAddon corsFilterAddon = new CorsFilterAddon();

    @Override
    public CdiModule getCdiModule() {
        return cdiModule
                .register(ResponseCorsFilter.class);
    }
}
