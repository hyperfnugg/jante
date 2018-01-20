package no.obos.util.servicebuilder.addon;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JettyServer;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.PropertyProvider;
import no.obos.util.servicebuilder.util.ExceptionUtil;
import org.eclipse.jetty.webapp.WebAppContext;
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
    public void addToJettyServer(JettyServer jettyServer) {
        WebAppContext webAppContext;
        webAppContext = new WebAppContext();
        URI parsedResourceUri = ExceptionUtil.wrapCheckedExceptions(() -> new URI(this.resourceUri));
        String warUrlString;
        String scheme = parsedResourceUri.getScheme();
        if (scheme == null) {
            throw new IllegalStateException("URI did not contain scheme: " + parsedResourceUri.toString());
        }
        String path = parsedResourceUri.getSchemeSpecificPart();
        path = (path.startsWith("//")) ? path.substring(2) : path;
        switch (scheme) {
            case "file":
                webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
                LOGGER.warn("*** Kjører i DEV-modus, leser webfiler rett fra utviklingskataloger. ***");
                warUrlString = path;
                File f = new File(warUrlString);
                if (!f.exists()) {
                    throw new IllegalStateException("Could not find file " + path);
                }
                break;
            case "classpath":
                final URL warUrl = WebAppAddon.class.getClassLoader().getResource(path);
                if (warUrl == null) {
                    throw new NullPointerException();
                }
                warUrlString = warUrl.toExternalForm();
                break;
            default:
                throw new IllegalArgumentException("Unrecognized URI scheme " + scheme + ". Allowed: classpath, file");
        }
        webAppContext.setResourceBase(warUrlString);
        String contextPath = Joiner.on('/')
                .skipNulls()
                .join(jettyServer.configuration.contextPath, pathSpec);
        webAppContext.setContextPath(contextPath);
        webAppContext.setParentLoaderPriority(true);
        jettyServer.addAppContext(webAppContext);
    }

    public WebAppAddon pathSpec(String pathSpec) {
        return withPathSpec(pathSpec);
    }


    public WebAppAddon resourceUri(String resourceUri) {
        return withResourceUri(resourceUri);
    }
}
