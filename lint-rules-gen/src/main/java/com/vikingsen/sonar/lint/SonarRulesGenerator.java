package com.vikingsen.sonar.lint;

import com.android.tools.lint.checks.BuiltinIssueRegistry;
import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.TextFormat;
import com.vikingsen.sonar.AndroidLintJavaRulesDefinition;
import com.vikingsen.sonar.AndroidLintResourcesRulesDefinition;
import com.vikingsen.sonar.lint.dto.DtoProfile;
import com.vikingsen.sonar.lint.dto.DtoRule;
import com.vikingsen.sonar.lint.dto.DtoRules;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SonarRulesGenerator {

    private static final int ERROR_CRITICAL_PRIORITY = 7;
    private static final int WARNING_MAJOR_PRIORITY = 7;

    private static final String RESOURCES_RULES_REPOSITORY_KEY = AndroidLintResourcesRulesDefinition.KEY;
    private static final String RESOURCES_PROFILE_NAME = "Android Lint Resources";
    private static final String RESOURCES_REPOSITORY_LANGUAGE = AndroidLintResourcesRulesDefinition.LANGUAGE;

    private static final String JAVA_RULES_REPOSITORY_KEY = AndroidLintJavaRulesDefinition.KEY;
    private static final String JAVA_PROFILE_NAME = "Android Lint Java";
    private static final String JAVA_REPOSITORY_LANGUAGE = AndroidLintJavaRulesDefinition.LANGUAGE;

    private static final String JAVA_SUFFIX = ".java";

    private static final File BASE_OUT_DIRECTORY = new File("out");
    private static final File RESOURCES_OUT_DIRECTORY = new File(BASE_OUT_DIRECTORY, "resources");
    private static final File JAVA_OUT_DIRECTORY = new File(BASE_OUT_DIRECTORY, "java");
    private static final String PROFILE_FILE = "profile.xml";
    private static final String RULES_FILE = "rules.xml";

    private DtoRules javaRules = new DtoRules();
    private DtoRules resRules = new DtoRules();
    private DtoProfile javaProfile = new DtoProfile();
    private DtoProfile resProfile = new DtoProfile();

    private Logger log = LoggerFactory.getLogger(SonarRulesGenerator.class);

    public void genRules() {
        javaProfile.setName(JAVA_PROFILE_NAME);
        javaProfile.setLanguage(JAVA_REPOSITORY_LANGUAGE);
        resProfile.setName(RESOURCES_PROFILE_NAME);
        resProfile.setLanguage(RESOURCES_REPOSITORY_LANGUAGE);

        List<Issue> issues = getIssues();
        for(Issue issue : issues) {
            processIssue(issue);
        }

        // Output files
        Serializer serializer = new Persister();
        File resourceRulesRepoFile = new File(RESOURCES_OUT_DIRECTORY, RULES_FILE);
        //noinspection ResultOfMethodCallIgnored
        resourceRulesRepoFile.getParentFile().mkdirs();
        File javaRulesRepoFile = new File(JAVA_OUT_DIRECTORY, RULES_FILE);
        //noinspection ResultOfMethodCallIgnored
        javaRulesRepoFile.getParentFile().mkdirs();
        File resourceProfileFile = new File(RESOURCES_OUT_DIRECTORY, PROFILE_FILE);
        //noinspection ResultOfMethodCallIgnored
        resourceProfileFile.getParentFile().mkdirs();
        File javaProfileFile = new File(JAVA_OUT_DIRECTORY, PROFILE_FILE);
        //noinspection ResultOfMethodCallIgnored
        javaProfileFile.getParentFile().mkdirs();
        try {
            serializer.write(resRules, resourceRulesRepoFile);
            serializer.write(javaRules, javaRulesRepoFile);
            serializer.write(resProfile, resourceProfileFile);
            serializer.write(javaProfile, javaProfileFile);
        } catch (Exception e) {
            log.error("Failed to write files", e);
        }
    }

    private List<Issue> getIssues() {
        IssueRegistry registry = new BuiltinIssueRegistry();
        List<Issue> sorted = new ArrayList<>(registry.getIssues());
        Collections.sort(sorted, new Comparator<Issue>() {
            @Override
            public int compare(Issue issue1, Issue issue2) {
                int d = issue1.getCategory().compareTo(issue2.getCategory());
                if (d != 0) {
                    return d;
                }
                d = issue2.getPriority() - issue1.getPriority();
                if (d != 0) {
                    return d;
                }

                return issue1.getId().compareTo(issue2.getId());
            }
        });
        return sorted;
    }


    private void processIssue(Issue issue) {
        boolean addToJavaRules = false;
        boolean addToResRules = false;
        for (Scope scope : issue.getImplementation().getScope()) {
            if (isJava(scope)) {
                addToJavaRules = true;
            } else {
                addToResRules = true;
            }
            if (addToJavaRules && addToResRules) {
                break;
            }
        }

        if (addToJavaRules) {
            DtoRule dtoRule = genRule(issue, true);
            javaRules.addRule(dtoRule);
            if (issue.isEnabledByDefault()) {
                javaProfile.addRule(JAVA_RULES_REPOSITORY_KEY, dtoRule.getKey());
            }
        }
        if (addToResRules) {
            DtoRule dtoRule = genRule(issue, false);
            resRules.addRule(dtoRule);
            if (issue.isEnabledByDefault()) {
                resProfile.addRule(RESOURCES_RULES_REPOSITORY_KEY, dtoRule.getKey());
            }
        }
    }

    private DtoRule genRule(Issue issue, boolean forJava) {
        DtoRule dtoRule = new DtoRule();
        dtoRule.setKey(issue.getId() + (forJava ? JAVA_SUFFIX : ""));
        dtoRule.setName(issue.getId());
        dtoRule.setDescription(wrapInCData(issue.getBriefDescription(TextFormat.HTML), issue.getExplanation(TextFormat.HTML)));
        dtoRule.setSeverity(getSeverity(issue));
        addTags(dtoRule, issue.getCategory());
        return dtoRule;
    }

    private String wrapInCData(String... htmlStrings) {
        StringBuilder builder = new StringBuilder();
        for (String html : htmlStrings) {
            builder.append(html).append("<br/>");
        }
        return  builder.toString();
    }

    private void addTags(DtoRule dtoRule, @Nonnull Category category) {
        if (category.getParent() != null) {
            addTags(dtoRule, category.getParent());
        }
        dtoRule.addTag(category.getName());
    }

    private SonarSeverity getSeverity(Issue issue) {
        switch (issue.getDefaultSeverity()) {
            case FATAL:
                return SonarSeverity.BLOCKER;
            case ERROR:
                if (issue.getPriority() <= ERROR_CRITICAL_PRIORITY) {
                    return SonarSeverity.CRITICAL;
                } else {
                    return SonarSeverity.MAJOR;
                }
            case WARNING:
                if (issue.getPriority() <= WARNING_MAJOR_PRIORITY) {
                    return SonarSeverity.MAJOR;
                } else {
                    return SonarSeverity.MINOR;
                }
            case INFORMATIONAL:
            case IGNORE:
            default:
                return SonarSeverity.INFO;
        }
    }

    private boolean isJava(Scope scope) {
        switch (scope) {
            case JAVA_FILE:
            case ALL_JAVA_FILES:
            case CLASS_FILE:
            case ALL_CLASS_FILES:
            case JAVA_LIBRARIES:
                return true;
        }
        return false;
    }
}
