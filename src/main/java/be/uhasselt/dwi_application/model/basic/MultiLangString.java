package be.uhasselt.dwi_application.model.basic;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.HashMap;

@JsonDeserialize(as = MultiLangString.class)
public class MultiLangString {
    private Map<String, String> values = new HashMap<>();

    public MultiLangString() {}

    public MultiLangString(Map<String, String> values) {
        this.values = values;
    }

    @JsonAnyGetter
    public Map<String, String> getValues() {
        return values;
    }

    @JsonAnySetter
    public void setValue(String languageCode, String text) {
        this.values.put(languageCode, text);
    }

    @JsonIgnore
    public String getValue(String languageCode) {
        return values.getOrDefault(languageCode, "nl");
    }

    @Override
    public String toString() {
        return "MultiLangString{" + "values=" + values + '}';
    }
}

