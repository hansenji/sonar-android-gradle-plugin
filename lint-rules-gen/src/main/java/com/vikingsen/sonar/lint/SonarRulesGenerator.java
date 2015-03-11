package com.vikingsen.sonar.lint;

import com.android.tools.lint.checks.BuiltinIssueRegistry;
import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.TextFormat;
import com.vikingsen.sonar.AndroidLintRulesDefinition;
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

    private static final String RULES_REPOSITORY_KEY = AndroidLintRulesDefinition.KEY;
    private static final String PROFILE_NAME = "Android Lint";
    private static final String REPOSITORY_LANGUAGE = AndroidLintRulesDefinition.LANGUAGE;

    private static final File PROFILE_FILE = new File("out", "profile.xml");
    private static final File RULES_FILE = new File("out", "rules.xml");

    private DtoRules rules = new DtoRules();
    private DtoProfile profile = new DtoProfile();

    private Logger log = LoggerFactory.getLogger(SonarRulesGenerator.class);

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void genRules() {
        profile.setName(PROFILE_NAME);
        profile.setLanguage(REPOSITORY_LANGUAGE);

        List<Issue> issues = getIssues();
        for (Issue issue : issues) {
            processIssue(issue);
        }

        // Output files
        Serializer serializer = new Persister();
        RULES_FILE.getParentFile().mkdirs();
        PROFILE_FILE.getParentFile().mkdirs();
        try {
            serializer.write(rules, RULES_FILE);
            serializer.write(profile, PROFILE_FILE);
        } catch (Exception e) {
            log.error("Failed to write files", e);
        }

        // Print Stats
        log.info("{} total rules", rules.getRules().size());
        log.info("{} rule(s) in profile", profile.getRules().size());
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
        DtoRule dtoRule = new DtoRule();
        dtoRule.setKey(issue.getId());
        dtoRule.setName(issue.getBriefDescription(TextFormat.TEXT));
        dtoRule.setDescription(issue.getExplanation(TextFormat.HTML));
        dtoRule.setSeverity(getSeverity(issue));
        addTags(dtoRule, issue.getCategory());
        rules.addRule(dtoRule);
        if (issue.isEnabledByDefault()) {
            profile.addRule(RULES_REPOSITORY_KEY, dtoRule.getKey());
        }
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
                if (issue.getPriority() >= ERROR_CRITICAL_PRIORITY) {
                    return SonarSeverity.CRITICAL;
                } else {
                    return SonarSeverity.MAJOR;
                }
            case WARNING:
                if (issue.getPriority() >= WARNING_MAJOR_PRIORITY) {
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
}
