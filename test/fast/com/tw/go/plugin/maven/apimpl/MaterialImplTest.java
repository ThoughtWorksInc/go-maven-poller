package com.tw.go.plugin.maven.apimpl;


import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRepositoryConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRepositoryPoller;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class MaterialImplTest {
    @Test
    public void shouldGetMavenRepositoryConfig() {
        MaterialImpl repositoryMaterial = new MaterialImpl();
        PackageRepositoryConfiguration repositoryConfiguration = repositoryMaterial.getConfig();
        assertThat(repositoryConfiguration, is(notNullValue()));
        assertThat(repositoryConfiguration, instanceOf(PluginConfig.class));
    }

    @Test
    public void shouldGetMavenRepositoryPoller() {
        MaterialImpl repositoryMaterial = new MaterialImpl();
        PackageRepositoryPoller poller = repositoryMaterial.getPoller();
        assertThat(poller, is(notNullValue()));
        assertThat(poller, instanceOf(PollerImpl.class));
    }
}
