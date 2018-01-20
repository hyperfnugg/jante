package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestServiceRunnerJetty;

import static no.obos.util.servicebuilder.TestServiceRunnerJetty.testServiceRunnerJetty;

abstract class AddonTestBase {

    TestServiceRunnerJetty testServiceRunnerJettyWithDefaults(ServiceConfig serviceConfig) {
        return testServiceRunnerJetty(serviceConfig)
                .property("server.port", "0")
                .property("service.version", "1.0")
                .property("server.contextPath", "/test/v1.0");
    }
}
