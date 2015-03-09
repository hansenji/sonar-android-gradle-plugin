package com.vikingsen.sonar.language;

import org.sonar.api.resources.AbstractLanguage;

public class AndroidResources extends AbstractLanguage {

    public static final String KEY = "androidresources";
    private static final String NAME = "Android Resources";
    private static final String[] SUFFIXES = {".xml"};

    public AndroidResources() {
        super(KEY, NAME);
    }

    @Override
    public String[] getFileSuffixes() {
        return SUFFIXES;
    }
}
