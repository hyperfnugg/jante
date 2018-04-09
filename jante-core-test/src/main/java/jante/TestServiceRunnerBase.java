package jante;

public interface TestServiceRunnerBase {
    TestServiceRunnerBase withStartedRuntime();

    TestServiceRunnerBase serviceConfig(ServiceConfig serviceConfig);

    ServiceConfig getServiceConfig();

    TestRuntime getRuntime();
}
