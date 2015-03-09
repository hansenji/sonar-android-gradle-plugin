package com.vikingsen.sonar;

import com.vikingsen.sonar.language.AndroidResources;
import org.apache.commons.io.IOUtils;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;

import java.io.InputStreamReader;

public class AndroidLintResourcesRulesDefinition implements RulesDefinition {

    public static final String KEY = "androidLintResources";
    public static final String LANGUAGE = AndroidResources.KEY;
    private static final String NAME = "Android Lint Resources Rules Repository";
    private static final String XML_PATH = "/com/vikingsen/sonar/plugins/lint/resources/rules.xml";
    private RulesDefinitionXmlLoader xmlLoader;

    public AndroidLintResourcesRulesDefinition(RulesDefinitionXmlLoader xmlLoader) {
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
