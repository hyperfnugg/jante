package jante.model;

public interface ServiceDefinition {

    String getName();

    Version getVersion();

    Iterable<Class> getResources();

    default SerializationSpec getSerializationSpec() {
        return SerializationSpec.standard;
    }

}
