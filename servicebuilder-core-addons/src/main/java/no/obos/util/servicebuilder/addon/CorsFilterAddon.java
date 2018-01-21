package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.CdiModule;
import no.obos.util.servicebuilder.cors.ResponseCorsFilter;
import no.obos.util.servicebuilder.model.Addon;

import static no.obos.util.servicebuilder.CdiModule.cdiModule;

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
