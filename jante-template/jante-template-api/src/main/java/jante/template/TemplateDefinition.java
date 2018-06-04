package jante.template;


import jante.model.ServiceDefinition;
import jante.model.Version;
import jante.template.resources.TemplateResource;

import java.util.Collections;

import static java.util.Collections.singletonList;

public class TemplateDefinition implements ServiceDefinition {
    public static final String NAME = "template";
    public static final Version VERSION = new Version(1, 0, 0);
    public static final Iterable<Class> RESOURCES = singletonList(TemplateResource.class);

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

    @Override
    public Iterable<Class> getResources() {
        return RESOURCES;
    }

    public static final TemplateDefinition templateDefinition = new TemplateDefinition();
}
