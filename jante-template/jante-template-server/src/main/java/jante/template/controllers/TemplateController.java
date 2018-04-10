package jante.template.controllers;

import jante.template.model.Template;

import java.util.List;

public interface TemplateController {

    List<Template> getAll();

    Template get(int id);

    Template create(Template payload);

    void update(int id, Template payload);

    void delete(int i);
}
