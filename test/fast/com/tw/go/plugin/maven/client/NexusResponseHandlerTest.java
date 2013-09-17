package com.tw.go.plugin.maven.client;

import com.tw.go.plugin.maven.LookupParams;
import com.tw.go.plugin.util.HttpRepoURL;
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
        NexusResponseHandler nexusResponseHandler = new NexusResponseHandler(responseBody);
        assertThat(nexusResponseHandler.getFilesMatching(lookupParams.getArtifactSelectionPattern()).get(0), is("book_inventory-1.0.0-18.war"));
    }

    @Test
    public void shouldReportCorrectLocationOfJarFile() throws IOException {
        String repoUrl = "https://repository.jboss.org/nexus/content/groups/public/";
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL(repoUrl, null, null),
                "jboss", "jboss-aop", "jar", null, null, null);
        String responseBody = FileUtils.readFileToString(new File("test/fast/jboss-dir.xml"));
        NexusResponseHandler nexusResponseHandler = new NexusResponseHandler(responseBody);
        List<Version> list = nexusResponseHandler.getAllVersions();
        MavenRepositoryClient mavenRepositoryClient = new MavenRepositoryClient(lookupParams);
        RepositoryConnector repoConnector = mock(RepositoryConnector.class);
        mavenRepositoryClient.setRepositoryConnector(repoConnector);
        Version result = mavenRepositoryClient.getLatest(list);
        String filesUrl = repoUrl + "jboss/jboss-aop/2.0.0.alpha2/";
        when(repoConnector.getFilesUrl(lookupParams, result.getV_Q())).thenReturn(filesUrl);
        String filesResponse = FileUtils.readFileToString(new File("test/fast/jboss-files.xml"));
        when(repoConnector.makeFilesRequest(lookupParams, result.getV_Q())).thenReturn(filesResponse);
        String location = mavenRepositoryClient.getFiles(result).getArtifactLocation();
        assertThat(location, is("https://repository.jboss.org/nexus/content/groups/public/jboss/jboss-aop/2.0.0.alpha2/jboss-aop-2.0.0.alpha2.jar"));
    }
}
