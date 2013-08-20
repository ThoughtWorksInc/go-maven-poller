package com.tw.go.plugin.maven.config;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfigurations;
import com.thoughtworks.go.plugin.api.response.validation.Errors;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MavenPackageConfigTest {
    @Test
    public void shouldValidateBounds(){
        PackageConfigurations packageConfigs = new PackageConfigurations();
        packageConfigs.addConfiguration(new PackageConfiguration(MavenPackageConfig.GROUP_ID, "com.tw"));
        packageConfigs.addConfiguration(new PackageConfiguration(MavenPackageConfig.ARTIFACT_ID, "mypkg"));
        packageConfigs.addConfiguration(new PackageConfiguration(MavenPackageConfig.POLL_VERSION_FROM, "1.2"));
        packageConfigs.addConfiguration(new PackageConfiguration(MavenPackageConfig.POLL_VERSION_TO, "1.5"));
        MavenPackageConfig mavenPackageConfig = new MavenPackageConfig(packageConfigs);
        Errors errors = new Errors();
        mavenPackageConfig.validate(errors);
        assertFalse(errors.hasErrors());
    }
    @Test
    public void shouldRejectInvalidBounds(){
        PackageConfigurations packageConfigs = new PackageConfigurations();
        packageConfigs.addConfiguration(new PackageConfiguration(MavenPackageConfig.GROUP_ID, "com.tw"));
        packageConfigs.addConfiguration(new PackageConfiguration(MavenPackageConfig.ARTIFACT_ID, "mypkg"));
        packageConfigs.addConfiguration(new PackageConfiguration(MavenPackageConfig.POLL_VERSION_FROM, "1.2"));
        packageConfigs.addConfiguration(new PackageConfiguration(MavenPackageConfig.POLL_VERSION_TO, "1.1"));
        MavenPackageConfig mavenPackageConfig = new MavenPackageConfig(packageConfigs);
        Errors errors = new Errors();
        mavenPackageConfig.validate(errors);
        assertTrue(errors.hasErrors());
        ValidationError error = errors.getErrors().get(0);
        assertThat(error.getMessage(), is(MavenPackageConfig.INVALID_BOUNDS_MESSAGE));
    }

    @Test
    public void shouldRejectEqualBounds(){
        PackageConfigurations packageConfigs = new PackageConfigurations();
        packageConfigs.addConfiguration(new PackageConfiguration(MavenPackageConfig.GROUP_ID, "com.tw"));
        packageConfigs.addConfiguration(new PackageConfiguration(MavenPackageConfig.ARTIFACT_ID, "mypkg"));
        packageConfigs.addConfiguration(new PackageConfiguration(MavenPackageConfig.POLL_VERSION_FROM, "1.2"));
        packageConfigs.addConfiguration(new PackageConfiguration(MavenPackageConfig.POLL_VERSION_TO, "1.2"));
        MavenPackageConfig mavenPackageConfig = new MavenPackageConfig(packageConfigs);
        Errors errors = new Errors();
        mavenPackageConfig.validate(errors);
        assertTrue(errors.hasErrors());
        ValidationError error = errors.getErrors().get(0);
        assertThat(error.getMessage(), is(MavenPackageConfig.INVALID_BOUNDS_MESSAGE));
    }

}