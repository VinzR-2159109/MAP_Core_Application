package be.uhasselt.dwi_application.model.basic;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Image {
    @JsonProperty("object")
    private final String object = "image"; // Constant field

    @JsonProperty("smallUrl")
    private String smallUrl;

    @JsonProperty("mediumUrl")
    private String mediumUrl;

    @JsonProperty("originalUrl")
    private String originalUrl;

    public Image() {}

    public Image(String smallUrl, String mediumUrl, String originalUrl) {
        this.smallUrl = smallUrl;
        this.mediumUrl = mediumUrl;
        this.originalUrl = originalUrl;
    }

    public String getSmallUrl() {
        return smallUrl;
    }

    public void setSmallUrl(String smallUrl) {
        this.smallUrl = smallUrl;
    }

    public String getMediumUrl() {
        return mediumUrl;
    }

    public void setMediumUrl(String mediumUrl) {
        this.mediumUrl = mediumUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    @Override
    public String toString() {
        return "Image{" +
                "object='" + object + '\'' +
                ", smallUrl='" + smallUrl + '\'' +
                ", mediumUrl='" + mediumUrl + '\'' +
                ", originalUrl='" + originalUrl + '\'' +
                '}';
    }
}
