package be.uhasselt.dwi_application.model.basic;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Property {
    @JsonProperty("object")
    private final String object = "property"; // Constant field

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private MultiLangString name; // Multi-language property name

    public Property() {}

    public Property(String id, MultiLangString name) {
        this.id = id;
        this.name = name;
    }

    public String getObject() {
        return object;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MultiLangString getName() {
        return name;
    }

    public void setName(MultiLangString name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Property{" +
                "object='" + object + '\'' +
                ", id='" + id + '\'' +
                ", name=" + name +
                '}';
    }
}
