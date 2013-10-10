package com.tw.go.plugin.maven.apimpl;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.maven.config.LookupParams;
import com.tw.go.plugin.util.HttpRepoURL;
import com.tw.go.plugin.util.RepoUrl;

public class MavenPollerTest {
//    @Test
    public void testCheckPackage(){
        LookupParams params = new LookupParams(
                (HttpRepoURL) RepoUrl.create("http://localhost:2201/nexus/content/repositories/releases/",
                        "deployment", "deployment123"),"com.thoughtworks.studios.go","book_management","war",null, null,null);
        PackageRevision packageRevision = new MavenPoller().poll(params);
        System.out.println(packageRevision.getRevision());
    }
}
//todo: test for unauthorized 401 response
//todo: checkPackage should also check artifact extn
//todo: add mssg/warning as env var if location not found
//todo: create my pkg rev class
//todo: better use of httpclient
//todo: separate out MavenVersion from Version
//todo: move Version and Strings to utils
//todo: try to deseralize just enough pom
/*
* artifactory:
* base url not enough, need to specify repo as well
* artifactory:http:  ?? or separate plugin?
* useful REST API only in pro version
* may need an artifact version search followed by a GAVC search
* */