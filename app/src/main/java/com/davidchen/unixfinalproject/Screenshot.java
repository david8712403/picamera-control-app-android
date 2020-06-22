package com.davidchen.unixfinalproject;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.LocalDateTime;

class Screenshot implements Serializable {
    @SerializedName("name")
    private String name;
    @SerializedName("createAt")
    private String createAt;
    @SerializedName("url")
    private String url;

    public Screenshot(String name, String dateTime, String url) {
        this.name = name;
        this.createAt = dateTime;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateTime() {
        return createAt;
    }

    public void setDateTime(String dateTime) {
        this.createAt = dateTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
