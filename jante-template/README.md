<!-- __MARKER_START__ : Everything from line with marker start to marker end is excluded from prototype -->

Project is used as basis for archetype. Most resources are copied to new project.

Post processing steps performed in script
```src/main/resources/META-INF/archetype-post-generate.groovy```

Notable behavior:
* Strings "Template" and "template" are replaced according to generation-time parameters
* parts of README.md and jante-tamplate/pom.xml are filtered
* .gitignore file has workaround in archetype_gitignore (https://issues.apache.org/jira/browse/ARCHETYPE-505)

To generate project from archetype:


```
mvn archetype:generate \
-B \
-DarchetypeGroupId=jante.template \
-DarchetypeArtifactId=jante-template-archetype \
-DarchetypeVersion=0.1.0-SNAPSHOT \
-Dversion=1.0-SNAPSHOT \
-DCapitalizedResource=Bar \
-DlowerCaseResource=bar \
-DgroupId=bar \
-DartifactId=bar
```




_Only the section of the file after this point will be included in generated project_
<!-- __MARKER_END__ : Everything from line with marker start to marker end is excluded from prototype -->
TODO: write nice readme for template
