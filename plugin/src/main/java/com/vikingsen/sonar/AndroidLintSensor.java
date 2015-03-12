package com.vikingsen.sonar;

import com.vikingsen.sonar.dto.DtoIssue;
import com.vikingsen.sonar.dto.DtoIssues;
import com.vikingsen.sonar.dto.DtoLocation;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;

import java.io.File;
import java.io.IOException;

public class AndroidLintSensor implements Sensor {

    private final FileSystem fileSystem;
    private final FilePredicates predicates;
    private final ResourcePerspectives perspectives;
    private final RulesProfile rulesProfile;

    private final Logger log = LoggerFactory.getLogger(AndroidLintSensor.class);
    private final File lintXml;

    public AndroidLintSensor(FileSystem fileSystem, ResourcePerspectives perspectives, RulesProfile rulesProfile, Settings settings) {
        this.fileSystem = fileSystem;
        this.predicates = fileSystem.predicates();
        this.perspectives = perspectives;
        this.rulesProfile = rulesProfile;
        lintXml = getFile(settings.getString(AndroidLintPlugin.ANDROID_LINT_REPORT));
        log.debug("Looking for Lint Report at {}", lintXml.getPath());
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        boolean execute = lintXml.exists();
        if (!execute) {
            log.info("Lint report was not found at {}. Skipping Android Lint", lintXml.getPath());
            log.debug("Set {} if lint-results.xml is not at {}.", AndroidLintPlugin.ANDROID_LINT_REPORT, AndroidLintPlugin.ANDROID_LINT_REPORT_DEFAULT);
        }
        return execute;
    }

    @Override
    public void analyse(Project project, SensorContext sensorContext) {
        Serializer serializer = new Persister();
        try {
            DtoIssues dtoIssues = serializer.read(DtoIssues.class, lintXml);
            for (DtoIssue dtoIssue : dtoIssues.getIssueList()) {
                processIssue(sensorContext, dtoIssue);
            }
        } catch (Exception e) {
            log.error("Exception reading " + lintXml.toString(), e);
        }
    }

    private void processIssue(SensorContext sensorContext, DtoIssue dtoIssue) {
        ActiveRule rule = rulesProfile.getActiveRule(AndroidLintRulesDefinition.KEY, dtoIssue.getId());
        if (rule != null) {
            log.debug("Processing Issue: {}", dtoIssue.getId());
            for (DtoLocation dtoLocation : dtoIssue.getLocations()) {
                processIssueForLocation(rule, dtoIssue, dtoLocation);
            }
        } else {
            log.debug("Unable to find rule for {}", dtoIssue.getId());
        }
    }

    private void processIssueForLocation(ActiveRule rule, DtoIssue dtoIssue, DtoLocation dtoLocation) {
        InputFile inputFile = fileSystem.inputFile(predicates.hasAbsolutePath(dtoLocation.getFile()));
        if (inputFile != null) {
            Issuable issuable = perspectives.as(Issuable.class, inputFile);
            if (issuable != null) {
                Issue issue = issuable.newIssueBuilder()
                        .ruleKey(rule.getRule().ruleKey())
                        .message(dtoIssue.getMessage())
                        .line(dtoLocation.getLine())
                        .build();
                issuable.addIssue(issue);
            }
        }
    }

    @Override
    public String toString() {
        return AndroidLintSensor.class.getSimpleName();
    }

    private File getFile(String path) {
        File file = new File(path);
        if (!file.isAbsolute()) {
            try {
                file = new File(fileSystem.baseDir(), path).getCanonicalFile();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to resolve path \'" + path + "\'", e);
            }
        }
        return file;
    }
}
