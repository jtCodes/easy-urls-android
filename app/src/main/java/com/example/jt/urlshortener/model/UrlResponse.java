package com.example.jt.urlshortener.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "shorten",
        "original"
})
public class UrlResponse {

    @JsonProperty("shorten")
    private String shorten;
    @JsonProperty("original")
    private String original;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("shorten")
    public String getShorten() {
        return shorten;
    }

    @JsonProperty("shorten")
    public void setShorten(String shorten) {
        this.shorten = shorten;
    }

    @JsonProperty("original")
    public String getOriginal() {
        return original;
    }

    @JsonProperty("original")
    public void setOriginal(String original) {
        this.original = original;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}