package jante.addon;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import jante.JettyServer;
import jante.model.Addon;
import jante.model.PropertyProvider;
import jante.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URL;

/**
 * Legger serving av statiske filer. Standard path er tjeneste/versjon/webapp/ .
 * Lokasjon av statiske filer kan spesifiseres med file:// (relativ path på filsystemet) eller classpath:// .
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WebAppAddon implements Addon {
    public static final String CONFIG_KEY_RESOURCE_URL = "webapp.resource.url";
    static final Logger LOGGER = LoggerFactory.getLogger(WebAppAddon.class);

    @Wither(AccessLevel.PRIVATE)
    public final String pathSpec;

    @Wither(AccessLevel.PRIVATE)
    public final String resourceUri;

    public static WebAppAddon webAppAddon = new WebAppAddon("/webapp/*", "classpath://webapp");


    @Override
    public Addon withProperties(PropertyProvider properties) {
        return this.resourceUri(properties.requireWithFallback(CONFIG_KEY_RESOURCE_URL, resourceUri));
    }


    @Override
    public JettyServer addToJettyServer(JettyServer jettyServer) {
        URI parsedResourceUri = ExceptionUtil.wrapCheckedExceptions(() -> new URI(this.resourceUri));
        String resourceUrlString;
        String scheme = getScheme(parsedResourceUri);
        String path = getPath(parsedResourceUri);
        boolean hotReload = "file".equals(scheme);
        resourceUrlString = getResourceUrlString(scheme, path);


        return jettyServer.addStaticResources(resourceUrlString, hotReload, pathSpec);
    }


    private String getPath(URI parsedResourceUri) {
        String path = parsedResourceUri.getSchemeSpecificPart();
        path = (path.startsWith("//")) ? path.substring(2) : path;
        return path;
    }

    private String getScheme(URI parsedResourceUri) {
        String scheme = parsedResourceUri.getScheme();
        if (scheme == null) {
            throw new IllegalStateException("URI did not contain scheme: " + parsedResourceUri.toString());
        }
        return scheme;
    }

    private String getResourceUrlString(String scheme, String path) {
        String resourceUrlString;
        switch (scheme) {
            case "file":
                resourceUrlString = path;
                File f = new File(resourceUrlString);
                if (!f.exists()) {
                    throw new IllegalStateException("Could not find file " + path);
                }
                break;
            case "classpath":
                final URL warUrl = WebAppAddon.class.getClassLoader().getResource(path);
                if (warUrl == null) {
                    throw new NullPointerException();
                }
                resourceUrlString = warUrl.toExternalForm();
                break;
            default:
                throw new IllegalArgumentException("Unrecognized URI scheme " + scheme + ". Allowed: classpath, file");
        }
        return resourceUrlString;
    }

    public WebAppAddon pathSpec(String pathSpec) {
        return withPathSpec(pathSpec);
    }


    public WebAppAddon resourceUri(String resourceUri) {
        return withResourceUri(resourceUri);
    }
}
