package jante.template;

import jante.Addons;
import jante.ServiceConfig;
import jante.template.controllers.TemplateController;
import jante.template.controllers.TemplateControllerJdbi;
import jante.template.db.dao.TemplateDao;
import jante.template.resources.TemplateResource;
import jante.template.resources.TemplateResourceImpl;

import static jante.CdiModule.cdiModule;
import static jante.ServiceRunner.serviceRunner;
import static jante.addon.WebAppAddon.webAppAddon;
import static jante.config.PropertyMap.propertyFileFromJvmArgs;

public class Main {
    public final static ServiceConfig commonConfig = Addons.standardAddons(TemplateDefinition.instance)
            .addon(webAppAddon)
            .cdi(props -> cdiModule
                    .bind(TemplateResourceImpl.class, TemplateResource.class)
            );


    public final static ServiceConfig mainConfig = commonConfig
            .addon(Addons.h2InMemoryDatasource()
                    .script("CREATE TABLE template (id INTEGER PRIMARY KEY AUTO_INCREMENT, name VARCHAR, value DOUBLE, startDate DATE)")
            )
            .addon(Addons.jdbi()
                    .dao(TemplateDao.class)
            )
            .cdi(props -> cdiModule
                    .bind(TemplateControllerJdbi.class, TemplateController.class)
            );


    public static void main(String[] args) {
        serviceRunner(mainConfig)
                .properties(propertyFileFromJvmArgs())
                .start().join();
    }

}
