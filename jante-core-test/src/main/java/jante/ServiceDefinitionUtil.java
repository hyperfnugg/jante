package jante;

import com.google.common.collect.ImmutableList;
import jante.model.ServiceDefinition;
import jante.model.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import jante.model.ServiceDefinition;
import jante.model.Version;


public class ServiceDefinitionUtil {
    public static final String ANONYMOUS_SERVICE_NAME = "anonymous_service";

    public static ServiceDefinition stubServiceDefinition(final String name, final Class... resources) {
        ImmutableList<Class> classes = ImmutableList.copyOf(resources);
        return new TestServiceDefinition(name, classes);
    }

    public static ServiceDefinition stubServiceDefinition(final Class... resources) {
        ImmutableList<Class> classes = ImmutableList.copyOf(resources);
        return new TestServiceDefinition(classes);
    }

    @AllArgsConstructor
    public static class TestServiceDefinition implements ServiceDefinition {
        @Getter
        final String name;
        @Getter
        final ImmutableList<Class> resources;
        @Getter
        final Version version = new Version(1, 0, 0);

        TestServiceDefinition(ImmutableList<Class> resources) {

            this.name = ANONYMOUS_SERVICE_NAME;
            this.resources = resources;
        }

    }

}
