package com.tw.go.plugin.maven.config;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.maven.client.Version;
import com.tw.go.plugin.util.HttpRepoURL;


public class LookupParams {
    public static final String PACKAGE_LOCATION = "LOCATION";
    public static final String PACKAGE_VERSION = "VERSION";
    public static final String ANY = "ANY";
    private final String groupId;
    private final HttpRepoURL repoUrl;
    private final String artifactId;
    private final String artifactExtn;

    private String pollVersionFrom = ANY;
    private String pollVersionTo = ANY;
    private PackageRevision lastKnownVersion = null;

    public LookupParams(HttpRepoURL repoUrl, String groupId, String artifactId, String artifactExtn, String pollVersionFrom, String pollVersionTo, PackageRevision previouslyKnownRevision) {
        this.repoUrl = repoUrl;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.artifactExtn = artifactExtn;
        if (pollVersionFrom != null && !pollVersionFrom.trim().isEmpty()) this.pollVersionFrom = pollVersionFrom;
        if (pollVersionTo != null && !pollVersionTo.trim().isEmpty()) this.pollVersionTo = pollVersionTo;
        this.lastKnownVersion = previouslyKnownRevision;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getRepoUrlStr() {
        return repoUrl.getUrlStr();
    }

    public boolean isLastVersionKnown() {
        return lastKnownVersion != null;
    }

    public String getLastKnownVersion() {
        if (lastKnownVersion == null) return null;
        return lastKnownVersion.getDataFor(PACKAGE_VERSION);
    }

    public boolean lowerBoundGiven() {
        return !ANY.equals(pollVersionFrom);
    }

    public boolean upperBoundGiven() {
        return !ANY.equals(pollVersionTo);
    }

    public String getPassword() {
        return repoUrl.getCredentials().getPassword();
    }

    public String getUsername() {
        return repoUrl.getCredentials().getUser();
    }

    public String getArtifactSelectionPattern() {
        return String.format(".*\\.%s$", artifactExtn);
    }

    public Version getUpperBound() {
        return new Version(pollVersionTo);
    }

    public Version lowerBound() {
        return new Version(pollVersionFrom);
    }

    public String getRepoUrlStrWithBasicAuth() {
        return repoUrl.getUrlWithBasicAuth();
    }
}
