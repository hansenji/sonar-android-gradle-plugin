package com.vikingsen.sonar.lint;

public class Main {
    public static void main(String[] args) {
        SonarRulesGenerator generator = new SonarRulesGenerator();
        generator.genRules();
    }
}
