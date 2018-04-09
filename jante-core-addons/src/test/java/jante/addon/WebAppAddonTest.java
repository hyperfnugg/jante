package jante.addon;

import jante.ServiceConfig;
import jante.TestService;
import org.assertj.core.util.Files;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.File;

import static jante.TestServiceRunnerJetty.testServiceRunnerJetty;
import static jante.addon.WebAppAddon.webAppAddon;
import static org.assertj.core.api.Assertions.assertThat;

public class WebAppAddonTest {

    @Test
    public void serves_from_classpath() {

        ServiceConfig serviceConfig = TestService.config
                .addon(webAppAddon);
        Response call = testServiceRunnerJetty(serviceConfig)
                .property("server.port", "0")
                .property("webapp.resource.url", "classpath:webapp")
                .oneShot(target -> target
                        .path("webapp")
                        .path("page.html")
                        .request()
                        .get()
                );
        String expected = "Yes!\n";
        assertThat(call.readEntity(String.class)).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    public void serves_from_filesystem() {

        String webAppDirLocation = "file:src/test/resources/webapp";
        File file = Files.currentFolder();

        if (file.getName().equals("jante")) {
            webAppDirLocation = "file:jante-core-addons/src/test/resources/webapp";
        }

        ServiceConfig serviceConfig = TestService.config
                .addon(webAppAddon);
        Response call = testServiceRunnerJetty(serviceConfig)
                .property("server.port", "0")
                .property("webapp.resource.url", webAppDirLocation)
                .oneShot(target -> target
                        .path("webapp")
                        .path("page.html")
                        .request()
                        .get()
                );
        String expected = "Yes!\n";
        assertThat(call.readEntity(String.class)).isEqualToIgnoringWhitespace(expected);
    }

}

