package jante;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.collect.ImmutableList;
import jante.model.ServiceDefinition;
import jante.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import jante.model.ServiceDefinition;
import jante.util.JsonUtil;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;


public class JerseyConfig {

    @Getter
    final ResourceConfig resourceConfig = new ResourceConfig();


    private void registerServiceDefintion(ServiceDefinition serviceDefinition) {
        serviceDefinition.getResources().forEach(resourceConfig::register);

        ObjectMapper mapper = JsonUtil.createObjectMapper(serviceDefinition.getSerializationSpec());
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(mapper);
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(provider);
    }

    public JerseyConfig(ServiceDefinition serviceDefinition, ImmutableList<CdiModule> cdiModules) {
        resourceConfig.property("jersey.config.server.wadl.disableWadl", "true");
        registerServiceDefintion(serviceDefinition);

        cdiModules.forEach(cdi ->
                cdi.registrators.forEach(registrator -> registrator.applyRegistations(resourceConfig))
        );

        resourceConfig.register(new InjectionBinder(cdiModules));
    }


    public interface Binder {
        void addBindings(AbstractBinder binder);
    }


    public interface Registrator {
        void applyRegistations(ResourceConfig resourceConfig);
    }


    @AllArgsConstructor
    class InjectionBinder extends AbstractBinder {
        final ImmutableList<CdiModule> cdiModules;

        @Override
        protected void configure() {
            cdiModules.forEach(cdi ->
                    cdi.binders.forEach(binder -> binder.addBindings(this))
            );
        }
    }
}
