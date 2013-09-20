package com.tw.go.plugin.maven.client;

import com.tw.go.plugin.maven.nexus.ContentItem;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ContentItemTest {
    @Test
    public void shouldConvertToVersion(){
        ContentItem item = new ContentItem();
        String lastModified = "2013-03-18 12:55:15.0 UTC";
        item.setLastModified(lastModified);
        item.setLeaf("false");
        item.setSizeOnDisk("-1");
        item.setResourceURI("http://nexus-server:8081/nexus/content/repositories/releases/com/thoughtworks/studios/go/book_inventory/1.0.0-15/");
        item.setRelativePath("/repositories/releases/com/thoughtworks/studios/go/book_inventory/1.0.0-15/");
        item.setText("1.0.0-15");
        Version v = item.toVersion();
        assertThat(v.getVersion(), is("1.0.0"));
        assertThat(v.getQualifier(), is("15"));
        assertThat(ContentItem.MAVEN_DATE_FORMAT.format(v.getLastModified()), is(lastModified));
    }
}
