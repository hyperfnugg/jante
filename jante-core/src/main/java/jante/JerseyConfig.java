package jante;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.collect.ImmutableList;
import jante.model.ServiceDefinition;
import jante.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
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

    public JerseyConfig(ServiceDefinition serviceDefinition, ImmutableList<Injections> injectionsList) {
        resourceConfig.property("jersey.config.server.wadl.disableWadl", "true");
        registerServiceDefintion(serviceDefinition);

        injectionsList.forEach(inject ->
                inject.registrators.forEach(registrator -> registrator.applyRegistations(resourceConfig))
        );

        resourceConfig.register(new InjectionBinder(injectionsList));
    }


    @AllArgsConstructor
    class InjectionBinder extends AbstractBinder {
        final ImmutableList<Injections> injections;

        @Override
        protected void configure() {
            injections.forEach(injection ->
                    injection.binders.forEach(binder -> binder.addBindings(this))
            );
        }
    }
}
