package com.tw.go.plugin.maven.client;

import com.tw.go.plugin.maven.LookupParams;
import com.tw.go.plugin.util.HttpRepoURL;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MavenRepositoryClientTest {
    @Test
    public void getVersions() {
        Version result = new MavenRepositoryClient(
                new LookupParams(
                    new HttpRepoURL("http://nexus-server:8081/nexus/content/repositories/releases/", null, null),
                    "com.thoughtworks.studios.go", "book_inventory", "war", null, null, null, false)).getLatest();
        assertThat(result.getVersion(), is("1.0.0"));
        assertThat(result.getQualifier(), is("18"));
        assertThat(result.getLocation(), is("http://nexus-server:8081/nexus/content/repositories/releases/com/thoughtworks/studios/go/book_inventory/1.0.0-18/book_inventory-1.0.0-18.war"));

    }
}
