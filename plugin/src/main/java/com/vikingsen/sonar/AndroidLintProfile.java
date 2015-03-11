package com.vikingsen.sonar;

import org.apache.commons.io.IOUtils;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.profiles.XMLProfileParser;
import org.sonar.api.utils.ValidationMessages;

import java.io.InputStreamReader;

public class AndroidLintProfile extends ProfileDefinition {

    private static final String XML_PATH = "/com/vikingsen/sonar/plugins/lint/profile.xml";

    private final XMLProfileParser parser;

    public AndroidLintProfile(XMLProfileParser parser) {
        this.parser = parser;
    }

    @Override
    public RulesProfile createProfile(ValidationMessages validation) {
        InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(XML_PATH));
        try {
            return parser.parse(reader, validation);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }
}
