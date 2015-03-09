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
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.ActiveRule;

import java.io.File;

public class AndroidLintSensor implements Sensor {

    private static final String JAVA_SUFFIX = ".java";
    private static final String CLASS_SUFFIX = ".class";

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
        lintXml = getLintXmlFile(settings);
        log.debug("Looking for Lint Report at {}", lintXml.getPath());
    }

    private File getLintXmlFile(Settings settings) {
        boolean isAbsolute = settings.getBoolean(AndroidLintPlugin.ANDROID_LINT_REPORT_ABSOLUTE_PATH);
        String path = settings.getString(AndroidLintPlugin.ANDROID_LINT_REPORT);
        if (isAbsolute) {
            return new File(path);
        } else {
            return new File(fileSystem.baseDir(), path);
        }
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
        for (DtoLocation dtoLocation : dtoIssue.getLocations()) {
            boolean java = dtoLocation.getFile().endsWith(JAVA_SUFFIX) || dtoLocation.getFile().endsWith(CLASS_SUFFIX);
            ActiveRule rule = getRule(dtoIssue, java);
            if (rule != null) {
                log.debug("Processing Issue: {}{}", dtoIssue.getId(), java ? JAVA_SUFFIX : "");
                processIssueForLocation(sensorContext, rule, dtoIssue, dtoLocation);
            } else {
                log.debug("Unable to find rule for {}{}", dtoIssue.getId(), java ? JAVA_SUFFIX : "");
            }
        }
    }

    private ActiveRule getRule(DtoIssue dtoIssue, boolean java) {
        return rulesProfile.getActiveRule(java ? AndroidLintJavaRulesDefinition.KEY : AndroidLintResourcesRulesDefinition.KEY,
                dtoIssue.getId() + (java ? JAVA_SUFFIX : ""));
    }

    private void processIssueForLocation(SensorContext sensorContext, ActiveRule rule, DtoIssue dtoIssue, DtoLocation dtoLocation) {
        InputFile inputFile = fileSystem.inputFile(predicates.hasAbsolutePath(dtoLocation.getFile()));
        if (inputFile != null) {
            Resource resource = sensorContext.getResource(inputFile);
            Issuable issuable = perspectives.as(Issuable.class, resource);
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
}
