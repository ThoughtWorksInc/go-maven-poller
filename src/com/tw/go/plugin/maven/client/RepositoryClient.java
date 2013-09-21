package com.tw.go.plugin.maven.client;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.tw.go.plugin.maven.config.LookupParams;
import com.tw.go.plugin.maven.nexus.NexusResponseHandler;

import java.util.Collections;
import java.util.List;

public class RepositoryClient {

    private static final Logger LOGGER = Logger.getLoggerFor(RepositoryClient.class);
    private RepositoryConnector repositoryConnector = new RepositoryConnector();
    private LookupParams lookupParams;

    public RepositoryClient(LookupParams lookupParams) {
        this.lookupParams = lookupParams;
    }

    public Version getLatest() {
        RepoResponse repoResponse = repositoryConnector.makeAllVersionsRequest(lookupParams);
        LOGGER.debug(repoResponse.responseBody);
        List<Version> allVersions = getAllVersions(repoResponse);
        Version latest = getLatest(allVersions);
        if(latest != null){
            latest.setArtifactId(lookupParams.getArtifactId());
            latest.setGroupId(lookupParams.getGroupId());
            LOGGER.info("Latest is "+latest.getRevisionLabel());
            setLocationAndTrackBack(latest);
        }else{
            LOGGER.warn("getLatest returning null");
        }
        return latest;
    }

    private void setLocationAndTrackBack(Version version) {
        try{
            Files files = getFiles(version);
            version.setLocation(files.getArtifactLocation());
            version.setTrackBackUrl(files.getTrackBackUrl());
        }catch(Exception ex){
            LOGGER.error("Error getting location for " + version.getRevisionLabel());
            if(ex.getMessage() != null)
                LOGGER.error(ex.getMessage());
            if(ex.getCause() != null && ex.getCause().getMessage() != null){
                LOGGER.error(ex.getCause().getMessage());
            }
            version.setErrorMessage("Plugin could not determine location/trackback. Please see plugin log for details.");
        }
    }

    Version getLatest(List<Version> allVersions) {
        if(allVersions == null || allVersions.isEmpty()) return null;
        Version latest = maxSubjectToUpperBound(allVersions);
        if(latest == null) {
            LOGGER.info("maxSubjectToUpperBound is null");
            return null;
        }
        if (lookupParams.isLastVersionKnown()) {
            LOGGER.info("lastKnownVersion is "+ lookupParams.getLastKnownVersion());
            Version lastKnownVersion = new Version(lookupParams.getLastKnownVersion());
            if (noNewerVersion(latest, lastKnownVersion)) {
                LOGGER.info("no newer version");
                return null;
            }
        }
        if(!lookupParams.lowerBoundGiven() || latest.greaterOrEqual(lookupParams.lowerBound())){
            return latest;
        }else{
            LOGGER.info("latestSubjectToLowerBound is null");
            return null;
        }
    }

    private Version maxSubjectToUpperBound(List<Version> allVersions) {
        Version absoluteMax = Collections.max(allVersions);
        if(!lookupParams.upperBoundGiven()) return absoluteMax;
        Collections.sort(allVersions);
        for(int i = 0; i < allVersions.size(); i++){
            if(allVersions.get(i).lessThan(lookupParams.getUpperBound()) &&
                    i+1 <= allVersions.size()-1 &&
                    allVersions.get(i+1).greaterOrEqual(lookupParams.getUpperBound()))
                return allVersions.get(i);
            if(allVersions.get(i).lessThan(lookupParams.getUpperBound()) &&
                    i+1 == allVersions.size())
                return allVersions.get(i);
        }
        return null;
    }

    private boolean noNewerVersion(Version latest, Version lastKnownVersion) {
        return latest.notNewerThan(lastKnownVersion);
    }

    private List<Version> getAllVersions(RepoResponse repoResponse) {
        List<Version> versions;
        NexusResponseHandler nexusReponseHandler = new NexusResponseHandler(repoResponse);
        if (nexusReponseHandler.canHandle()) {
            versions = nexusReponseHandler.getAllVersions();
        } else {
            LOGGER.warn("Falling back to HTML parsing as the Nexus XML structure was not found");
            HtmlResponseHandler htmlResponseHandler = new HtmlResponseHandler(repoResponse);
            versions = htmlResponseHandler.getAllVersions();
        }
        return versions;
    }

    Files getFiles(Version version) {
        RepoResponse repoResponse = repositoryConnector.makeFilesRequest(lookupParams, version.getV_Q());
        LOGGER.debug(repoResponse.responseBody);
        NexusResponseHandler nexusReponseHandler = new NexusResponseHandler(repoResponse);
        List<String> files;
        String pomFile = null;
        if (nexusReponseHandler.canHandle()) {
            files = nexusReponseHandler.getFilesMatching(lookupParams.getArtifactSelectionPattern());
            pomFile = nexusReponseHandler.getPOMfile();
        } else {
            HtmlResponseHandler htmlResponseHandler = new HtmlResponseHandler(repoResponse);
            LOGGER.warn("Falling back to HTML parsing as the Nexus XML structure was not found");
            files = htmlResponseHandler.getFiles(lookupParams.getArtifactSelectionPattern());
        }
        return new Files(repositoryConnector.getFilesUrlWithBasicAuth(lookupParams, version.getV_Q()),
                files.get(0), pomFile);
    }

    void setRepositoryConnector(RepositoryConnector repositoryConnector) {
        this.repositoryConnector = repositoryConnector;
    }
}
