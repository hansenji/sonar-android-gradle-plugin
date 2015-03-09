package com.vikingsen.sonar;

import com.vikingsen.sonar.language.AndroidResources;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import java.util.Arrays;
import java.util.List;

/**
 * This class is the entry point for all extensions
 */
public final class AndroidLintPlugin extends SonarPlugin {

    private static final String ANDROID_LINT_CATEGORY = "Android Lint";
    private static final String GENERAL_SUBCATEGORY = "General";

    public static final String ANDROID_LINT_REPORT = "sonar.androidLintReport";
    public static final String ANDROID_LINT_REPORT_DEFAULT = "build/outputs/lint-results.xml";
    public static final String ANDROID_LINT_REPORT_ABSOLUTE_PATH = "sonar.androidLintReportAbsolutePath";
    public static final boolean ANDROID_LINT_REPORT_ABSOLUTE_PATH_DEFAULT = false;

    // This is where you're going to declare all your Sonar extensions
    public List getExtensions() {
        return Arrays.asList(
                // Language
                AndroidResources.class,

                // Properties
                PropertyDefinition.builder(ANDROID_LINT_REPORT)
                        .defaultValue(String.valueOf(ANDROID_LINT_REPORT_DEFAULT))
                        .category(ANDROID_LINT_CATEGORY)
                        .subCategory(GENERAL_SUBCATEGORY)
                        .name("Lint Report Path")
                        .description("Path to the lint xml report.")
                        .type(PropertyType.STRING)
                        .onQualifiers(Qualifiers.PROJECT)
                        .build(),

                PropertyDefinition.builder(ANDROID_LINT_REPORT_ABSOLUTE_PATH)
                        .defaultValue(String.valueOf(ANDROID_LINT_REPORT_ABSOLUTE_PATH_DEFAULT))
                        .category(ANDROID_LINT_CATEGORY)
                        .subCategory(GENERAL_SUBCATEGORY)
                        .name("Lint Report Path Is Absolute")
                        .description("Whether the lint report path is absolute.")
                        .type(PropertyType.BOOLEAN)
                        .onQualifiers(Qualifiers.PROJECT)
                        .build(),

                // Rules
                AndroidLintResourcesRulesDefinition.class,
                AndroidLintJavaRulesDefinition.class,

                // Profiles
                AndroidLintJavaProfile.class,
                AndroidLintResourcesProfile.class,

                // Sensors
                AndroidLintSensor.class
                );
    }

}
