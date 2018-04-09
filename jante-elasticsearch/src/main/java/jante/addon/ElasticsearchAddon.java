package jante.addon;

import jante.model.Addon;
import org.elasticsearch.client.Client;

public interface ElasticsearchAddon extends Addon {

    String getClustername();

    Client getClient();

    boolean isUnitTest();
}
