package com.tw.go.plugin.maven;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.maven.client.Version;
import com.tw.go.plugin.util.RepoUrl;


public class LookupParams {
    public static final String PACKAGE_LOCATION = "LOCATION";
    public static final String PACKAGE_VERSION = "VERSION";
    public static final String ANY = "ANY";
    private final String groupId;
    private final RepoUrl repoUrl;
    private final String artifactId;
    private final String artifactExtn;

    private String pollVersionFrom = ANY;
    private String pollVersionTo = ANY;
    private PackageRevision lastKnownVersion = null;
    private boolean includeSnapshots = true;

    public LookupParams(RepoUrl repoUrl, String groupId, String artifactId, String artifactExtn, String pollVersionFrom, String pollVersionTo, PackageRevision previouslyKnownRevision, boolean includeSnapshotsVersions) {
        this.repoUrl = repoUrl;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.artifactExtn = artifactExtn;
        if (pollVersionFrom != null && !pollVersionFrom.trim().isEmpty()) this.pollVersionFrom = pollVersionFrom;
        if (pollVersionTo != null && !pollVersionTo.trim().isEmpty()) this.pollVersionTo = pollVersionTo;
        this.lastKnownVersion = previouslyKnownRevision;
        this.includeSnapshots = includeSnapshotsVersions;
    }

    public String getRepoId() {
        return repoUrl.getRepoId();
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getRepoUrlStr() {
        return repoUrl.forDisplay();
    }

    public RepoUrl getRepoUrl() {
        return repoUrl;
    }

    public boolean isHttp() {
        return repoUrl.isHttp();
    }

    public String getRepoUrlStrWithTrailingSlash() {
        if (repoUrl.forDisplay().endsWith("/")) return repoUrl.forDisplay();
        return repoUrl.forDisplay() + "/";
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

    public String getPackageAndVersion() {
        if(eitherBoundGiven())
        return String.format("%s, %s to %s", groupId, displayVersion(pollVersionFrom), displayVersion(pollVersionTo));
        return groupId;
    }

    private String displayVersion(String version) {
        if(ANY.equals(version)) return ANY;
        return "V" + version;
    }

    private String getEffectiveLowerBound() {
        if (getLastKnownVersion() != null) return getLastKnownVersion();
        if (lowerBoundGiven()) return pollVersionFrom;
        return "0.0.1";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LookupParams that = (LookupParams) o;

        if (includeSnapshots != that.includeSnapshots) return false;
        if (lastKnownVersion != null ? !lastKnownVersion.equals(that.lastKnownVersion) : that.lastKnownVersion != null)
            return false;
        if (!groupId.equals(that.groupId)) return false;
        if (pollVersionFrom != null ? !pollVersionFrom.equals(that.pollVersionFrom) : that.pollVersionFrom != null)
            return false;
        if (pollVersionTo != null ? !pollVersionTo.equals(that.pollVersionTo) : that.pollVersionTo != null)
            return false;
        if (!repoUrl.equals(that.repoUrl)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + repoUrl.hashCode();
        result = 31 * result + (pollVersionFrom != null ? pollVersionFrom.hashCode() : 0);
        result = 31 * result + (pollVersionTo != null ? pollVersionTo.hashCode() : 0);
        result = 31 * result + (lastKnownVersion != null ? lastKnownVersion.hashCode() : 0);
        result = 31 * result + (includeSnapshots ? 1 : 0);
        return result;
    }

    public boolean eitherBoundGiven() {
        return upperBoundGiven() || lowerBoundGiven();
    }

    public boolean shoudIncludeSnapshots() {
        return includeSnapshots;
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
}
