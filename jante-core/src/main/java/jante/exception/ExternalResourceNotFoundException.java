package jante.exception;

public class ExternalResourceNotFoundException extends ExternalResourceException {
    public ExternalResourceNotFoundException(MetaData metaData, Exception ex) {
        super(metaData, ex);
    }

    public ExternalResourceNotFoundException(MetaData metaData) {
        super(metaData);
    }
}
