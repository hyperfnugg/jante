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
import static jante.addon.ElasticsearchMockAddon.elasticsearchMockAddon;
import static jante.addon.ExceptionMapperAddon.exceptionMapperAddon;
import static jante.addon.H2InMemoryDatasourceAddon.h2InMemoryDatasourceAddon;
import static jante.addon.JdbiAddon.jdbiAddon;
import static jante.addon.JerseyClientAddon.jerseyClientAddon;
import static jante.addon.QueryRunnerAddon.queryRunnerAddon;
import static jante.addon.ServerLogAddon.serverLogAddon;
import static jante.addon.SwaggerAddon.swaggerAddon;
import static jante.addon.WebAppAddon.webAppAddon;

public class Addons {

    public static CorsFilterAddon cors() {
        return CorsFilterAddon.corsFilterAddon;
    }

    public static ExceptionMapperAddon exceptionMapper() {
        return ExceptionMapperAddon.exceptionMapperAddon;
    }

    public static JerseyClientAddon jerseyClient(ServiceDefinition serviceDefinition) {
        return JerseyClientAddon.jerseyClientAddon(serviceDefinition);
    }


    public static SwaggerAddon swagger() {
        return SwaggerAddon.swaggerAddon;
    }

    public static WebAppAddon webAppAddon() {
        return WebAppAddon.webAppAddon;
    }

    public static ServerLogAddon serverLog() {
        return ServerLogAddon.serverLogAddon;
    }


    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>jante-db-basicdatasource</artifactId>
        </dependency>
     */
    public static BasicDatasourceAddon basicDatasource() {
        return basicDatasourceAddon;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>jante-db-h2</artifactId>
        </dependency>
     */
    public static H2InMemoryDatasourceAddon h2InMemoryDatasource() {
        return h2InMemoryDatasourceAddon;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>jante-db-jdbi</artifactId>
        </dependency>
     */
    public static JdbiAddon jdbi() {
        return JdbiAddon.jdbiAddon;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>jante-db-queryrunner</artifactId>
        </dependency>
     */
    public static QueryRunnerAddon queryRunner() {
        return queryRunnerAddon;
    }


    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>jante-activemq</artifactId>
        </dependency>
    */
    public static ActiveMqListenerAddon activeMqListener(Class<? extends MessageHandler> handler) {
        return ActiveMqListenerAddon.defaults(handler);
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>jante-activemq</artifactId>
        </dependency>
    */
    public static ActiveMqSenderAddon activeMqSender() {
        return ActiveMqSenderAddon.activeMqSenderAddon;
    }

    /*
    <dependency>
        <groupId>no.obos.util</groupId>
        <artifactId>jante-elasticsearch</artifactId>
    </dependency>
    */
    public static ElasticsearchClientAddon elasticsearch() {
        return ElasticsearchClientAddon.elasticsearchClientAddon;
    }

    /*
    <dependency>
        <groupId>no.obos.util</groupId>
        <artifactId>jante-elasticsearch-mock</artifactId>
    </dependency>
    */
    public static ElasticsearchMockAddon elasticsearchMock() {
        return elasticsearchMockAddon;
    }

    /*
    <dependency>
        <groupId>no.obos.util</groupId>
        <artifactId>jante-elasticsearch</artifactId>
    </dependency>
    */
    public static ElasticsearchIndexAddon elasticsearchIndex(String indexName, Class<?> indexedType) {
        return ElasticsearchIndexAddon.elasticsearchIndexAddon(indexName, indexedType);
    }


    public static ServiceConfig standardAddons(ServiceDefinition serviceDefinition) {
        return serviceConfig(serviceDefinition)
                .addon(SwaggerAddon.swaggerAddon)
                .addon(CorsFilterAddon.corsFilterAddon)
                .addon(RequestIdAddon.requestIdAddon)
                .addon(ExceptionMapperAddon.exceptionMapperAddon)
                .addon(ServerLogAddon.serverLogAddon)
                ;
    }
}
