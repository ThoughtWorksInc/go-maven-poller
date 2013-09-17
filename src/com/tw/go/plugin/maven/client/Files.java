package com.tw.go.plugin.maven.client;

import maven.Model;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.net.URL;

public class Files {
    private final String baseurl;
    private final String artifactLocation;
    private final String pomFile;
    private Model model;

    public Files(String baseurl, String artifactLocation, String pomFile) {
        this.baseurl = baseurl;
        this.artifactLocation = artifactLocation;
        this.pomFile = pomFile;
    }

    public String getArtifactLocation() {
        return baseurl + artifactLocation;
    }

    public String getTrackBackUrl() {
        if(model == null) getModel();
        return model.getUrl();
    }

    private void getModel() {
        try {
            model = Model.unmarshal(new InputSource(new URL(baseurl + pomFile).openStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
