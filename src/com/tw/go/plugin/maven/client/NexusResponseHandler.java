package com.tw.go.plugin.maven.client;

import com.thoughtworks.go.plugin.api.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public class NexusResponseHandler {
    private static final Logger LOGGER = Logger.getLoggerFor(NexusResponseHandler.class);
    private static final String FILES_TO_IGNORE = "^maven-metadata.*$|^archetype-catalog.*$|^.*sha1$|^.*md5$|^.*pom$";
    private final String responseBody;
    private Content content;

    public NexusResponseHandler(String responseBody) {
        this.responseBody = responseBody;
    }

    public boolean canHandle() {
        content = new Content().unmarshal(responseBody);
        return content != null;
    }

    public List<Version> getAllVersions() {
        List<Version> versions = new ArrayList<Version>();
        for (ContentItem ci : content.getContentItems()) {
            if (!ci.getText().matches(FILES_TO_IGNORE)) {
                Version version = ci.toVersion();
                versions.add(version);
            }
        }
        return versions;
    }
}
