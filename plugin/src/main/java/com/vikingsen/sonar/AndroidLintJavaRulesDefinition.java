package com.vikingsen.sonar;

import org.apache.commons.io.IOUtils;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;

import java.io.InputStreamReader;

public class AndroidLintJavaRulesDefinition implements RulesDefinition {

    public static final String KEY = "androidLintJava";
    public static final String LANGUAGE = "java"; // Better if it came from sonar
    private static final String NAME = "Android Lint Resources Rules Repository";
    private static final String XML_PATH = "/com/vikingsen/sonar/plugins/lint/java/rules.xml";
    private RulesDefinitionXmlLoader xmlLoader;

    public AndroidLintJavaRulesDefinition(RulesDefinitionXmlLoader xmlLoader) {
        this.xmlLoader = xmlLoader;
    }

    @Override
    public void define(Context context) {
        NewRepository repository = context.createRepository(KEY, LANGUAGE).setName(NAME);
        InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(XML_PATH));
        try {
            xmlLoader.load(repository, reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
        repository.done();
    }
}
