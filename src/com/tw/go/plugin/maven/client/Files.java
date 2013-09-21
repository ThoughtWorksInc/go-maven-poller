package com.tw.go.plugin.maven.client;

import com.tw.go.plugin.maven.config.LookupParams;
import maven.Model;
import org.xml.sax.InputSource;

import java.io.StringReader;

public class Files {
    private final String baseUrlWithAuth;
    private final String artifactLocation;
    private final String pomUrl;
    private final LookupParams lookupParams;
    private Model model;

    public Files(String baseUrlWithAuth, String artifactLocation, String pomUrl, LookupParams lookupParams) {
        this.baseUrlWithAuth = baseUrlWithAuth;
        this.artifactLocation = artifactLocation;
        this.pomUrl = pomUrl;
        this.lookupParams = lookupParams;
    }



    public String getArtifactLocation() {
        return baseUrlWithAuth + artifactLocation;
    }

    public String getTrackBackUrl() {
        if(model == null) getModel();
        return model.getUrl();
    }

    private void getModel() {
        RepoResponse repoResponse = new RepositoryConnector().doHttpRequest(lookupParams.getUsername(), lookupParams.getPassword(),
                pomUrl);
        model = Model.unmarshal(new InputSource(new StringReader(repoResponse.getResponseBody())));
    }
}
