package jante.template.resource;

import jante.Addons;
import jante.ServiceConfig;
import jante.TestServiceRunner;
import jante.addon.DataSourceAddon;
import jante.template.Main;
import jante.template.controllers.TemplateController;
import jante.template.dto.TemplateDto;
import jante.template.dto.TemplateNestedDto;
import jante.template.model.Template;
import jante.template.resources.TemplateResource;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import static java.util.Collections.singletonList;
import static jante.Injections.injections;
import static jante.TestServiceRunner.testServiceRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class TemplateResourceUnitTest {
    final ServiceConfig config = Main.commonConfig
            .removeAddon(DataSourceAddon.class)
            .addon(Addons.h2InMemoryDatasource()
                    .script("CREATE TABLE template (id INTEGER PRIMARY KEY AUTO_INCREMENT, name VARCHAR, value DOUBLE, startDate DATE)")
            );

    @Test
    public void insert() {
        //Given
        TemplateDto expectedDto = TemplateDto.builder()
                .name("foo")
                .startDate(LocalDate.MIN)
                .nested(TemplateNestedDto.builder()
                        .value(1.0)
                        .build()
                )
                .build();
        Template expected = Template.ofDto(expectedDto);
        TemplateController controller = Mockito.mock(TemplateController.class);
        TestServiceRunner runner = testServiceRunner(config
                .inject(props -> injections
                        .bind(controller, TemplateController.class)
                )
        );


        //When
        when(controller.getAll()).thenReturn(singletonList(expected));
        List<TemplateDto> actual = runner.oneShot(TemplateResource.class, TemplateResource::getAllTemplates);

        //Then
        assertThat(actual).isEqualTo(singletonList(expectedDto));
    }
}
