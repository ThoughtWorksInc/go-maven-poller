package com.tw.go.plugin.maven.client;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.maven.LookupParams;
import com.tw.go.plugin.util.HttpRepoURL;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MavenRepositoryClientTest {
    @Test
    public void shouldGetLatestVersion() throws IOException {
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("http://nexus-server:8081/nexus/content/repositories/releases/", null, null),
                "com.thoughtworks.studios.go", "book_inventory", "war", null, null, null, false);
        MavenRepositoryClient client = new MavenRepositoryClient(lookupParams);
        String responseBody = FileUtils.readFileToString(new File("test/fast/nexus-response.xml"));
        NexusResponseHandler nexusResponseHandler = new NexusResponseHandler(responseBody);
        Version result = client.getLatest(nexusResponseHandler.getAllVersions());
        assertThat(result.getVersion(), is("1.0.0"));
        assertThat(result.getQualifier(), is("18"));
    }

    @Test
    public void shouldReturnNullIfNoNewerVersion() throws IOException {
        PackageRevision previouslyKnownRevision = new PackageRevision("1.0.0-18", new Date(), "abc");
        previouslyKnownRevision.addData(LookupParams.PACKAGE_VERSION,"1.0.0-18");
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("http://nexus-server:8081/nexus/content/repositories/releases/", null, null),
                "com.thoughtworks.studios.go", "book_inventory", "war", null, null, previouslyKnownRevision, false);
        MavenRepositoryClient client = new MavenRepositoryClient(lookupParams);
        String responseBody = FileUtils.readFileToString(new File("test/fast/nexus-response.xml"));
        NexusResponseHandler nexusResponseHandler = new NexusResponseHandler(responseBody);
        assertNull(client.getLatest(nexusResponseHandler.getAllVersions()));
    }
}
