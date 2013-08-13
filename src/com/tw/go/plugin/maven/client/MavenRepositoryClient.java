package com.tw.go.plugin.maven.client;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.tw.go.plugin.maven.LookupParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles all communication with the Maven repository.
 *
 * @author mrumpf (of the original version at: https://github.com/mrumpf/repoclient-plugin)
 */
public class MavenRepositoryClient {
    private static final Pattern PATTERN = Pattern.compile(
            "href=[\n\r ]*\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);

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
        return getLatest(allVersions);

    }

    private Version getLatest(List<Version> allVersions) {
        Version latest = Collections.max(allVersions);
        latest.setLocation(getLocation(latest));
        latest.setArtifactId(lookupParams.getArtifactId());
        latest.setGroupId(lookupParams.getGroupId());
        return latest;
    }

    private List<Version> getAllVersions(String responseBody) {
        List<Version> versions = new ArrayList<Version>();
        NexusResponseHandler nexusReponseHandler = new NexusResponseHandler(responseBody);
        if (nexusReponseHandler.canHandle()) {
            versions = nexusReponseHandler.getAllVersions();
        } else {
            LOGGER.warn("Falling back to HTML parsing as the Nexus XML structure was not found");
            List<String> versionStrings = new ArrayList<String>();
            parseHtml(responseBody, versionStrings);
            for (String versionString : versionStrings)
                versions.add(new Version(versionString));
        }
        return versions;
    }

    public String getLocation(Version latest) {
        String baseurl = repositoryConnector.getFilesUrl(lookupParams, latest.getV_Q());
        String responseBody = repositoryConnector.makeFilesRequest(lookupParams, latest.getV_Q());
        List<String> files = new ArrayList<String>();
        Content c = new Content().unmarshal(responseBody);
        if (c != null) {
            for (ContentItem ci : c.getContentItems()) {
                if (ci.getText().matches(lookupParams.getArtifactSelectionPattern()))
                    files.add(ci.getText());
            }
        } else {
            files.clear();
            LOGGER.warn("Falling back to HTML parsing as the Nexus XML structure was not found");
            parseHtml(responseBody, files);
            for (String file : files) {
                if (file.matches(lookupParams.getArtifactSelectionPattern()))
                    files.add(file);
            }
        }
        return baseurl + files.get(0);
    }

    private void parseHtml(String responseBody, List<String> matches) {
        if (responseBody != null) {
            Matcher matcher = PATTERN.matcher(responseBody);
            while (matcher.find()) {
                String match = matcher.group(1);
                // remove trailing slash
                if (match.endsWith("/")) {
                    match = match.substring(0, match.length() - 1);
                }
                // extract the version only
                if (match.toLowerCase().startsWith("http")) {
                    int idx = match.lastIndexOf('/');
                    match = match.substring(0, idx);
                }
                if (!"..".equals(match)
                        && !match.toLowerCase().startsWith("http")) {
                    matches.add(match);
                }
            }
        }
    }
}
