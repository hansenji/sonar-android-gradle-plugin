package com.vikingsen.sonar.dto;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name="issue", strict = false)
public class DtoIssue {

    @Attribute
    private String id;
    @Attribute
    private String message;
    @ElementList(inline = true)
    private List<DtoLocation> locations;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<DtoLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<DtoLocation> dtoLocations) {
        this.locations = dtoLocations;
    }
}
