package com.tw.go.plugin.maven.apimpl;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfigurations;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.maven.LookupParams;
import com.tw.go.plugin.maven.config.MavenPackageConfig;
import com.tw.go.plugin.util.RepoUrl;
import org.junit.Test;

import java.util.Date;

import static org.mockito.Mockito.*;

public class PollerImplTest {
    @Test
    public void PollerShouldExcuteCorrectCmd(){
        PollerImpl poller = new PollerImpl();
        PollerImpl spy = spy(poller);
        PackageConfigurations repoCfgs = mock(PackageConfigurations.class);
        PackageConfigurations pkgCfgs = mock(PackageConfigurations.class);
        String repoUrlStr = "http://google.com";//something valid to satisfy connection check
        when(repoCfgs.get(RepoUrl.REPO_URL)).thenReturn(new PackageConfiguration(RepoUrl.REPO_URL, repoUrlStr));
        String user = "user";
        when(repoCfgs.get(RepoUrl.USERNAME)).thenReturn(new PackageConfiguration(RepoUrl.USERNAME, user));
        String password = "passwrod";
        when(repoCfgs.get(RepoUrl.PASSWORD)).thenReturn(new PackageConfiguration(RepoUrl.PASSWORD, password));
        String packageId = "7-Zip";
        PackageConfiguration packageConfiguration = new PackageConfiguration(MavenPackageConfig.GROUP_ID, packageId);
        when(pkgCfgs.get(MavenPackageConfig.GROUP_ID)).thenReturn(packageConfiguration);
        PackageRevision dummyResult = new PackageRevision("1.0", new Date(),"user");
        RepoUrl repoUrl = RepoUrl.create(repoUrlStr, user, password);
        LookupParams params = new LookupParams(repoUrl, "groupId", packageId,"war", null, null, null, true);
        doReturn(dummyResult).when(spy).poll(params);
        //actual test
        spy.getLatestRevision(pkgCfgs, repoCfgs);
        verify(spy).poll(params);
    }

}
