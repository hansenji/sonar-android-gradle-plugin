package com.vikingsen.sonar.lint.dto;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root(name = "rules")
public class DtoRules {
    @ElementList(inline = true)
    private List<DtoRule> rules = new ArrayList<>();

    public List<DtoRule> getRules() {
        return rules;
    }

    public void setRules(List<DtoRule> rules) {
        this.rules = rules;
    }

    public void addRule(DtoRule rule) {
        rules.add(rule);
    }
}
