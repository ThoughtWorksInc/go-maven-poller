package com.tw.go.plugin.maven.client;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.tw.go.plugin.maven.LookupParams;

import java.util.Collections;
import java.util.List;

/**
 * This class handles all communication with the Maven repository.
 *
 * @author mrumpf (of the original version at: https://github.com/mrumpf/repoclient-plugin)
 */
public class MavenRepositoryClient {

    private static final Logger LOGGER = Logger.getLoggerFor(MavenRepositoryClient.class);
    private final RepositoryConnector repositoryConnector = new RepositoryConnector();
    private LookupParams lookupParams;

    public MavenRepositoryClient(LookupParams lookupParams) {
        this.lookupParams = lookupParams;
    }

    public Version getLatest() {
        String responseBody = repositoryConnector.makeAllVersionsRequest(lookupParams);
        LOGGER.debug(responseBody);
        List<Version> allVersions = getAllVersions(responseBody);
        Version latest = getLatest(allVersions);
        latest.setLocation(getLocation(latest));
        latest.setArtifactId(lookupParams.getArtifactId());
        latest.setGroupId(lookupParams.getGroupId());
        return latest;
    }

    Version getLatest(List<Version> allVersions) {
        Version latest = Collections.max(allVersions);
        if (lookupParams.isLastVersionKnown()) {
            Version lastKnownVersion = new Version(lookupParams.getLastKnownVersion());
            if (noNewerVersion(latest, lastKnownVersion)) {
                return null;
            }
        }
        return latest;
    }

    private boolean noNewerVersion(Version latest, Version lastKnownVersion) {
        return latest.notNewerThan(lastKnownVersion);
    }

    private List<Version> getAllVersions(String responseBody) {
        List<Version> versions;
        NexusResponseHandler nexusReponseHandler = new NexusResponseHandler(responseBody);
        if (nexusReponseHandler.canHandle()) {
            versions = nexusReponseHandler.getAllVersions();
        } else {
            LOGGER.warn("Falling back to HTML parsing as the Nexus XML structure was not found");
            HtmlResponseHandler htmlResponseHandler = new HtmlResponseHandler(responseBody);
            versions = htmlResponseHandler.getAllVersions();
        }
        return versions;
    }

    public String getLocation(Version latest) {
        String baseurl = repositoryConnector.getFilesUrl(lookupParams, latest.getV_Q());
        String responseBody = repositoryConnector.makeFilesRequest(lookupParams, latest.getV_Q());
        LOGGER.debug(responseBody);
        NexusResponseHandler nexusReponseHandler = new NexusResponseHandler(responseBody);
        List<String> files;
        if (nexusReponseHandler.canHandle()) {
            files = nexusReponseHandler.getFiles(lookupParams.getArtifactSelectionPattern());
        } else {
            HtmlResponseHandler htmlResponseHandler = new HtmlResponseHandler(responseBody);
            LOGGER.warn("Falling back to HTML parsing as the Nexus XML structure was not found");
            files = htmlResponseHandler.getFiles(lookupParams.getArtifactSelectionPattern());
        }
        return baseurl + files.get(0);
    }


}
