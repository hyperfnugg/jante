package jante.template.resource;

import com.google.common.collect.Lists;
import jante.Addons;
import jante.ServiceConfig;
import jante.TestServiceRunner;
import jante.exception.ExternalResourceNotFoundException;
import jante.template.controllers.TemplateController;
import jante.template.controllers.TemplateControllerElasticsearch;
import jante.template.controllers.TemplateControllerInMemory;
import jante.template.dto.TemplateDto;
import jante.template.dto.TemplateNestedDto;
import jante.template.model.Template;
import jante.template.resources.TemplateResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static jante.CdiModule.cdiModule;
import static jante.TestServiceRunner.testServiceRunner;
import static jante.template.Main.commonConfig;
import static jante.template.Main.mainConfig;
import static org.assertj.core.api.Assertions.*;

@RunWith(Parameterized.class)
public class TemplateCrudForVariousBackends {
    final ServiceConfig config;
    final TestServiceRunner runner;

    public TemplateCrudForVariousBackends(ServiceConfig config) {
        this.config = config;
        runner = testServiceRunner(config);
    }

    @Parameterized.Parameters
    public static Collection<ServiceConfig> data() {
        return Lists.newArrayList(
                commonConfig
                        .cdi(props -> cdiModule
                                .bindSingleton(TemplateControllerInMemory.class, TemplateController.class)
                        )
                , mainConfig
                , commonConfig
                        .cdi(props -> cdiModule
                                .bindSingleton(TemplateControllerElasticsearch.class, TemplateController.class)
                        )
                        .addon(Addons.elasticsearchMock())
                        .addon(Addons.elasticsearchIndex("templateIndex", Template.class)
                                .doIndexing(true)
                        )
        );
    }


    public static final TemplateDto ORIGINAL = TemplateDto.builder()
            .name("Banan")
            .nested(TemplateNestedDto.builder().value(11.3).build())
            .build();

    @Test
    public void create() {
        runner.chain()
                .call(TemplateResource.class, resource ->
                        assertThat(resource.getAllTemplates())
                                .isEmpty()
                )
                .call(TemplateResource.class, resource -> resource.createTemplate(ORIGINAL))
                .call(TemplateResource.class, resource ->
                        assertThat(resource.getAllTemplates())
                                .hasSize(1)
                )
                .run()
        ;
    }

    @Test
    public void retreive() {
        runner.chain()
                .call(TemplateResource.class, resource -> resource.createTemplate(ORIGINAL))
                .call(TemplateResource.class, resource ->
                        assertThat(resource.getTemplate(1))
                                .isEqualTo(ORIGINAL.toBuilder().id(1).build())
                )
                .call(TemplateResource.class, resource ->
                        assertThatThrownBy(() -> resource.getTemplate(42))
                                .isExactlyInstanceOf(ExternalResourceNotFoundException.class)
                                .hasMessage("Target: template, Status: 404, Detail: No Template for id 42")

                )
                .run()
        ;
    }


    @Test
    public void update() {
        TemplateDto[] mutated = new TemplateDto[1];
        runner.chain()
                .call(TemplateResource.class, resource -> resource.createTemplate(ORIGINAL))
                .call(TemplateResource.class, resource -> {
                            TemplateDto template = resource.getTemplate(1);
                            mutated[0] = template.toBuilder()
                                    .name("Eple")
                                    .startDate(null)
                                    .build();
                            resource.updateTemplate(1, mutated[0]);
                        }
                )
                .call(TemplateResource.class, resource ->
                        assertThat(resource.getTemplate(1))
                                .isEqualTo(mutated[0])
                )
                .call(TemplateResource.class, resource ->
                        assertThatThrownBy(() -> resource.updateTemplate(42, mutated[0]))
                                .isExactlyInstanceOf(ExternalResourceNotFoundException.class)
                                .hasMessage("Target: template, Status: 404, Detail: No Template for id 42")
                )
                .run()
        ;
    }

    @Test
    public void delete() {
        runner.chain()
                .call(TemplateResource.class, resource -> resource.createTemplate(ORIGINAL))
                .call(TemplateResource.class, resource ->
                        assertThat(resource.getAllTemplates())
                                .hasSize(1)
                )
                .call(TemplateResource.class, resource -> resource.delete(1))
                .call(TemplateResource.class, resource ->
                        assertThat(resource.getAllTemplates())
                                .isEmpty()
                )
                .call(TemplateResource.class, resource ->
                        assertThatCode(() -> resource.delete(42))
                                .doesNotThrowAnyException()
                )
                .run()
        ;
    }
}
