package jante.es;

import org.elasticsearch.client.Client;

public class ElasticsearchUtil {
    public static String getClusterName(Client client) {
        return client.settings().get("cluster.name");
    }
}
