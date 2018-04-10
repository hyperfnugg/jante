package jante;

import jante.addon.*;
import jante.model.ServiceDefinition;
import jante.mq.MessageHandler;
import jante.addon.*;
import jante.model.ServiceDefinition;
import jante.mq.MessageHandler;

import static jante.ServiceConfig.serviceConfig;
import static jante.addon.BasicDatasourceAddon.basicDatasourceAddon;
import static jante.addon.CorsFilterAddon.corsFilterAddon;
import static jante.addon.ElasticsearchClientAddon.elasticsearchClientAddon;
import static jante.addon.ElasticsearchMockAddon.elasticsearchMockAddon;
import static jante.addon.ExceptionMapperAddon.exceptionMapperAddon;
import static jante.addon.H2InMemoryDatasourceAddon.h2InMemoryDatasourceAddon;
import static jante.addon.JdbiAddon.jdbiAddon;
import static jante.addon.JerseyClientAddon.jerseyClientAddon;
import static jante.addon.QueryRunnerAddon.queryRunnerAddon;
import static jante.addon.RequestIdAddon.requestIdAddon;
import static jante.addon.ServerLogAddon.serverLogAddon;
import static jante.addon.SwaggerAddon.swaggerAddon;
import static jante.addon.WebAppAddon.webAppAddon;

public class Addons {

    public static CorsFilterAddon cors() {
        return corsFilterAddon;
    }

    public static RequestIdAddon requestId() {
        return requestIdAddon;
    }

    public static ExceptionMapperAddon exceptionMapper() {
        return exceptionMapperAddon;
    }

    public static JerseyClientAddon jerseyClient(ServiceDefinition serviceDefinition) {
        return jerseyClientAddon(serviceDefinition);
    }


    public static SwaggerAddon swagger() {
        return swaggerAddon;
    }

    public static WebAppAddon webApp() {
        return webAppAddon;
    }

    public static ServerLogAddon serverLog() {
        return serverLogAddon;
    }


    /*
        <dependency>
            <groupId>jante</groupId>
            <artifactId>jante-db-basicdatasource</artifactId>
        </dependency>
     */
    public static BasicDatasourceAddon basicDatasource() {
        return basicDatasourceAddon;
    }

    /*
        <dependency>
            <groupId>jante</groupId>
            <artifactId>jante-db-h2</artifactId>
        </dependency>
     */
    public static H2InMemoryDatasourceAddon h2InMemoryDatasource() {
        return h2InMemoryDatasourceAddon;
    }

    /*
        <dependency>
            <groupId>jante</groupId>
            <artifactId>jante-db-jdbi</artifactId>
        </dependency>
     */
    public static JdbiAddon jdbi() {
        return jdbiAddon;
    }

    /*
        <dependency>
            <groupId>jante</groupId>
            <artifactId>jante-db-queryrunner</artifactId>
        </dependency>
     */
    public static QueryRunnerAddon queryRunner() {
        return queryRunnerAddon;
    }


    /*
        <dependency>
            <groupId>jante</groupId>
            <artifactId>jante-activemq</artifactId>
        </dependency>
    */
    public static ActiveMqListenerAddon activeMqListener(Class<? extends MessageHandler> handler) {
        return ActiveMqListenerAddon.defaults(handler);
    }

    /*
        <dependency>
            <groupId>jante</groupId>
            <artifactId>jante-activemq</artifactId>
        </dependency>
    */
    public static ActiveMqSenderAddon activeMqSender() {
        return ActiveMqSenderAddon.activeMqSenderAddon;
    }

    /*
    <dependency>
        <groupId>jante</groupId>
        <artifactId>jante-elasticsearch</artifactId>
    </dependency>
    */
    public static ElasticsearchClientAddon elasticsearch() {
        return elasticsearchClientAddon;
    }

    /*
    <dependency>
        <groupId>jante</groupId>
        <artifactId>jante-elasticsearch-mock</artifactId>
    </dependency>
    */
    public static ElasticsearchMockAddon elasticsearchMock() {
        return elasticsearchMockAddon;
    }

    /*
    <dependency>
        <groupId>jante</groupId>
        <artifactId>jante-elasticsearch</artifactId>
    </dependency>
    */
    public static ElasticsearchIndexAddon elasticsearchIndex(String indexName, Class<?> indexedType) {
        return ElasticsearchIndexAddon.elasticsearchIndexAddon(indexName, indexedType);
    }


    public static ServiceConfig standardAddons(ServiceDefinition serviceDefinition) {
        return serviceConfig(serviceDefinition)
                .addon(swaggerAddon)
                .addon(corsFilterAddon)
                .addon(requestIdAddon)
                .addon(exceptionMapperAddon)
                .addon(serverLogAddon)
                ;
    }
}
