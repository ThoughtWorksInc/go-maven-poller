package com.tw.go.plugin.maven.client;

import com.tw.go.plugin.maven.config.LookupParams;
import com.tw.go.plugin.maven.nexus.NexusResponseHandler;
import com.tw.go.plugin.util.HttpRepoURL;
import maven.MavenVersion;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NexusResponseHandlerTest {
    @Test
    public void shouldGetLatestVersionLocation() throws IOException {
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("http://nexus-server:8081/nexus/content/repositories/releases/", null, null),
                "com.thoughtworks.studios.go", "book_inventory", "war", null, null, null);
        String responseBody = FileUtils.readFileToString(new File("test/fast/nexus-files-response.xml"));
        NexusResponseHandler nexusResponseHandler = new NexusResponseHandler(new RepoResponse(responseBody, RepoResponse.APPLICATION_XML));
        assertThat(nexusResponseHandler.getFilesMatching(lookupParams.getArtifactSelectionPattern()).get(0), is("book_inventory-1.0.0-18.war"));
    }

    @Test
    public void shouldReportCorrectLocationOfJarFile() throws IOException {
        String repoUrlStr = "https://repository.jboss.org/nexus/content/groups/public/";
        HttpRepoURL httpRepoURL = new HttpRepoURL(repoUrlStr, "user", "pass");
        LookupParams lookupParams = new LookupParams(
                httpRepoURL,
                "jboss", "jboss-aop", "jar", null, null, null);
        String responseBody = FileUtils.readFileToString(new File("test/fast/jboss-dir.xml"));
        NexusResponseHandler nexusResponseHandler = new NexusResponseHandler(new RepoResponse(responseBody, RepoResponse.APPLICATION_XML));
        List<MavenVersion> list = nexusResponseHandler.getAllVersions();
        RepositoryClient repositoryClient = new RepositoryClient(lookupParams);
        RepositoryConnector repoConnector = mock(RepositoryConnector.class);
        repositoryClient.setRepositoryConnector(repoConnector);
        MavenVersion result = repositoryClient.getLatest(list);
        String filesUrl = httpRepoURL.getUrlWithBasicAuth() + "jboss/jboss-aop/2.0.0.alpha2/";
        when(repoConnector.getFilesUrlWithBasicAuth(lookupParams, result.getV_Q())).thenReturn(filesUrl);
        String filesResponse = FileUtils.readFileToString(new File("test/fast/jboss-files.xml"));
        when(repoConnector.makeFilesRequest(lookupParams, result.getV_Q())).thenReturn(new RepoResponse(filesResponse, RepoResponse.APPLICATION_XML));
        String location = repositoryClient.getFiles(result).getArtifactLocation();
        assertThat(location, is("https://user:pass@repository.jboss.org/nexus/content/groups/public/jboss/jboss-aop/2.0.0.alpha2/jboss-aop-2.0.0.alpha2.jar"));
    }
}
