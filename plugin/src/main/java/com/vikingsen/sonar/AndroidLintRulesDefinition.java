package com.vikingsen.sonar;

import org.apache.commons.io.IOUtils;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;

import java.io.InputStreamReader;

public class AndroidLintRulesDefinition implements RulesDefinition {

    public static final String KEY = "androidLint";
    public static final String LANGUAGE = "java"; // Better if it came from sonar
    private static final String NAME = "Android Lint Rules Repository";
    private static final String XML_PATH = "/com/vikingsen/sonar/plugins/lint/rules.xml";
    private RulesDefinitionXmlLoader xmlLoader;

    public AndroidLintRulesDefinition(RulesDefinitionXmlLoader xmlLoader) {
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
