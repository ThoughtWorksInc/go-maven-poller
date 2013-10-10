package maven;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import maven.Version;
import com.tw.go.plugin.maven.config.LookupParams;

import java.util.Date;

public class MavenVersion extends Version {
    private String artifactId = null;
    private String location = null;
    private String groupId = null;
    private String trackBackUrl = null;
    private String errorMessage;
    private Date lastModified = null;

    public MavenVersion(String ver) {
        super(ver);
    }

    public String getRevisionLabel() {
        return String.format("%s:%s.%s%s%s", groupId, artifactId, version, lastDelimiter, qualifier);
    }

    public PackageRevision toPackageRevision() {
        PackageRevision packageRevision = new PackageRevision(getRevisionLabel(), lastModified, null,null, trackBackUrl);
        packageRevision.addData(LookupParams.PACKAGE_LOCATION, location);
        packageRevision.addData(LookupParams.PACKAGE_VERSION, getV_Q());
        if(errorMessage != null)
            packageRevision.addData("ERRORMSG", errorMessage);
        return packageRevision;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setTrackBackUrl(String trackBackUrl) {
        this.trackBackUrl = trackBackUrl;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Date getLastModified() {
        return lastModified;
    }
}
