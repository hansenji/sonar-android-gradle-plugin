package com.vikingsen.sonar.dto;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name="location", strict = false)
public class DtoLocation {

    @Attribute
    private String file;

    @Attribute(required = false)
    private Integer line;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }
}
