package com.vikingsen.sonar.dto;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "issues", strict = false)
public class DtoIssues {
    @ElementList(inline = true)
    private List<DtoIssue> issueList;

    public List<DtoIssue> getIssueList() {
        return issueList;
    }

    public void setIssueList(List<DtoIssue> issueList) {
        this.issueList = issueList;
    }
}
