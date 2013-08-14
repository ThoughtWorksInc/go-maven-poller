package com.tw.go.plugin.maven.client;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlResponseHandler {
    private final String responseBody;

    public HtmlResponseHandler(String responseBody) {
        this.responseBody = responseBody;
    }

    public List<Version> getAllVersions() {
        List<String> versionStrings = new ArrayList<String>();
        List<Version> result = new ArrayList<Version>();
        parseHtml(versionStrings);
        for (String versionString : versionStrings)
            result.add(new Version(versionString));
        return result;
    }

    private static final Pattern HREF_URLS = Pattern.compile(
            "href=[\n\r ]*\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);

    private void parseHtml(List<String> matches) {
        if (responseBody != null) {
            Matcher matcher = HREF_URLS.matcher(responseBody);
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

    public List<String> getFiles(String artifactSelectionPattern) {
        List<String> files = new ArrayList<String>();
        parseHtml(files);
        for (String file : files) {
            if (file.matches(artifactSelectionPattern))
                files.add(file);
        }
        return files;
    }
}
