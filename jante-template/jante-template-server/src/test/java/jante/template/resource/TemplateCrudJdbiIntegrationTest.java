package jante.template.resource;

import jante.Addons;
import jante.ServiceConfig;
import jante.TestServiceRunner;
import jante.addon.DataSourceAddon;
import jante.template.Main;
import jante.template.db.dao.TemplateDao;
import jante.template.db.model.TemplateDb;
import jante.template.dto.TemplateDto;
import jante.template.dto.TemplateNestedDto;
import jante.template.model.Template;
import jante.template.resources.TemplateResource;
import org.junit.Test;

import java.time.LocalDate;

import static jante.TestServiceRunner.testServiceRunner;
import static org.assertj.core.api.Assertions.assertThat;

public class TemplateCrudJdbiIntegrationTest {

    final ServiceConfig config = Main.mainConfig
            .removeAddon(DataSourceAddon.class)
            .addon(Addons.h2InMemoryDatasource()
                    .script("CREATE TABLE template (id INTEGER PRIMARY KEY AUTO_INCREMENT, name VARCHAR, value DOUBLE, startDate DATE)")
            );

    final TestServiceRunner runner = testServiceRunner(config);

    static final TemplateDto original = TemplateDto.builder()
            .name("foobar")
            .startDate(LocalDate.MIN)
            .nested(TemplateNestedDto.builder()
                    .value(1.0)
                    .build()
            )
            .build();

    @Test
    public void create() {
        int[] id = new int[1];
        runner.chain()
                .call(TemplateResource.class, resource -> id[0] = resource.createTemplate(original))
                .injectee(TemplateDao.class, dao -> {
                    TemplateDto expectedDto = original.toBuilder().id(id[0]).build();
                    TemplateDb expected = TemplateDb.ofModel(Template.ofDto(expectedDto));

                    TemplateDb actal = dao.select(id[0]);
                    assertThat(actal).isEqualTo(expected);
                })
                .run();
    }
}
