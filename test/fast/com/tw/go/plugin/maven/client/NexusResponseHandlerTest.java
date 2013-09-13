package com.tw.go.plugin.maven.client;

import com.tw.go.plugin.maven.LookupParams;
import com.tw.go.plugin.util.HttpRepoURL;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NexusResponseHandlerTest {
    @Test
    public void shouldGetLatestVersionLocation() throws IOException {
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("http://nexus-server:8081/nexus/content/repositories/releases/", null, null),
                "com.thoughtworks.studios.go", "book_inventory", "war", null, null, null);
        String responseBody = FileUtils.readFileToString(new File("test/fast/nexus-files-response.xml"));
        NexusResponseHandler nexusResponseHandler = new NexusResponseHandler(responseBody);
        assertThat(nexusResponseHandler.getFiles(lookupParams.getArtifactSelectionPattern()).get(0), is("book_inventory-1.0.0-18.war"));
    }

    @Test
    public void shouldGetLatestVersionLocation2() throws IOException {
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("https://repository.jboss.org/nexus/content/groups/public/", null, null),
                "jboss", "jboss-aop", "jar", null, null, null);
        String responseBody = FileUtils.readFileToString(new File("test/fast/jboss-dir.xml"));
        NexusResponseHandler nexusResponseHandler = new NexusResponseHandler(responseBody);
        List<Version> list = nexusResponseHandler.getAllVersions();
        MavenRepositoryClient mavenRepositoryClient = new MavenRepositoryClient(lookupParams);
        Version result = mavenRepositoryClient.getLatest(list);
        String location = mavenRepositoryClient.getLocation(result);
        assertThat(location, is("https://repository.jboss.org/nexus/content/groups/public/jboss/jboss-aop/2.0.0.alpha2/jboss-aop-2.0.0.alpha2.jar"));
    }
}
