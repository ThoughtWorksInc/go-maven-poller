package com.tw.go.plugin.maven.client;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.tw.go.plugin.maven.config.LookupParams;
import com.tw.go.plugin.util.HttpRepoURL;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class RepositoryConnector {
    private static final Logger LOGGER = Logger.getLoggerFor(RepositoryConnector.class);

    public RepositoryConnector() {
    }

    public String concatUrl(String baseurl, String groupid,
                            String artifactid) {
        return concatUrl(baseurl, groupid, artifactid, "");
    }

    public String concatUrl(String baseurl, String groupid,
                            String artifactid, String version) {
        StringBuilder sb = new StringBuilder();
        sb.append(baseurl);
        if (!baseurl.endsWith("/")) {
            sb.append("/");
        }
        if (groupid.startsWith("/")) {
            sb.append(groupid.substring(1).replace('.', '/'));
        } else {
            sb.append(groupid.replace('.', '/'));
        }
        if (!groupid.endsWith("/")) {
            sb.append("/");
        }
        if (artifactid.startsWith("/")) {
            sb.append(artifactid.substring(1));
        } else {
            sb.append(artifactid);
        }
        if (!artifactid.endsWith("/")) {
            sb.append("/");
        }
        if (version.startsWith("/")) {
            sb.append(version.substring(1));
        } else {
            sb.append(version);
        }
        if (!version.isEmpty() && !version.endsWith("/")) {
            sb.append("/");
        }
        return sb.toString();
    }

    RepoResponse doHttpRequest(String username, String password,
                               String url) {
        HttpClient client = createHttpClient(username, password);

        String responseBody;
        HttpGet method = null;
        try {
            method = createGetMethod(url);
            HttpResponse response = client.execute(method);
            if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
                throw new RuntimeException(String.format("HTTP %s, %s",
                        response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
            }
            HttpEntity entity = response.getEntity();
            responseBody = EntityUtils.toString(entity);
            String mimeType = ContentType.get(entity).getMimeType();
            return new RepoResponse(responseBody, mimeType);
        } catch (Exception e) {
            String message = String.format("Exception while connecting to %s\n%s", url, e);
            LOGGER.error(message);
            throw new RuntimeException(message, e);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    HttpGet createGetMethod(String url) {
        HttpGet method = new HttpGet(url);
        method.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10 * 1000);
        method.setHeader("Accept", "application/xml");
        return method;
    }

    HttpClient createHttpClient(String username, String password) {
        DefaultHttpClient client = HttpRepoURL.getHttpClient();
        if (username != null) {
            Credentials creds = new UsernamePasswordCredentials(username, password);
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(AuthScope.ANY, creds);
            client.setCredentialsProvider(credsProvider);
        }
        return client;
    }

    public boolean testConnection(String url, String username,
                                  String password) {

        boolean result = false;
        HttpClient client = createHttpClient(username, password);

        HttpGet method = null;
        try {
            method = createGetMethod(url);

            HttpResponse response = client.execute(method);
            result = (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        } catch (Exception e) {
            String message = String.format("Exception while connecting to %s\n%s", url, e.getMessage());
            LOGGER.error(message);
            throw new RuntimeException(message, e);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
        return result;
    }

    public RepoResponse makeAllVersionsRequest(LookupParams lookupParams) {
        String url = concatUrl(lookupParams.getRepoUrlStr(), lookupParams.getGroupId(), lookupParams.getArtifactId());
        LOGGER.info("Getting versions from " + url);
        return doHttpRequest(lookupParams.getUsername(), lookupParams.getPassword(), url);
    }

    public RepoResponse makeFilesRequest(LookupParams lookupParams, String revision) {
        String baseurl = getFilesUrl(lookupParams, revision);
        LOGGER.debug("Getting files from " + baseurl);
        return doHttpRequest(lookupParams.getUsername(), lookupParams.getPassword(), baseurl);
    }

    public String getFilesUrl(LookupParams lookupParams, String revision) {
        return concatUrl(lookupParams.getRepoUrlStr(), lookupParams.getGroupId(), lookupParams.getArtifactId(), revision);
    }
    public String getFilesUrlWithBasicAuth(LookupParams lookupParams, String revision) {
        return concatUrl(lookupParams.getRepoUrlStrWithBasicAuth(), lookupParams.getGroupId(), lookupParams.getArtifactId(), revision);
    }
}