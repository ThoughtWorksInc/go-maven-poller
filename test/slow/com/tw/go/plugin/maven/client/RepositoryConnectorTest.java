package com.tw.go.plugin.maven.client;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class RepositoryConnectorTest {
    @Test
    public void testConn(){
        assertTrue(new RepositoryConnector().testConnection("https://repository.jboss.org/nexus/content/groups/public/", null, null));
    }
}
