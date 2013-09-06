package com.tw.go.plugin.maven.config;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.Property;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MavenPackageConfigTest {
    @Test
    public void shouldValidateBounds(){
        PackageConfiguration packageConfigs = new PackageConfiguration();
        packageConfigs.add(new Property(MavenPackageConfig.GROUP_ID, "com.tw"));
        packageConfigs.add(new Property(MavenPackageConfig.ARTIFACT_ID, "mypkg"));
        packageConfigs.add(new Property(MavenPackageConfig.POLL_VERSION_FROM, "1.2"));
        packageConfigs.add(new Property(MavenPackageConfig.POLL_VERSION_TO, "1.5"));
        MavenPackageConfig mavenPackageConfig = new MavenPackageConfig(packageConfigs);
        ValidationResult errors = new ValidationResult();
        mavenPackageConfig.validate(errors);
        assertTrue(errors.isSuccessful());
    }
    @Test
    public void shouldRejectInvalidBounds(){
        PackageConfiguration packageConfigs = new PackageConfiguration();
        packageConfigs.add(new Property(MavenPackageConfig.GROUP_ID, "com.tw"));
        packageConfigs.add(new Property(MavenPackageConfig.ARTIFACT_ID, "mypkg"));
        packageConfigs.add(new Property(MavenPackageConfig.POLL_VERSION_FROM, "1.2"));
        packageConfigs.add(new Property(MavenPackageConfig.POLL_VERSION_TO, "1.1"));
        MavenPackageConfig mavenPackageConfig = new MavenPackageConfig(packageConfigs);
        ValidationResult errors = new ValidationResult();
        mavenPackageConfig.validate(errors);
        assertFalse(errors.isSuccessful());
        ValidationError error = errors.getErrors().get(0);
        assertThat(error.getMessage(), is(MavenPackageConfig.INVALID_BOUNDS_MESSAGE));
    }

    @Test
    public void shouldRejectEqualBounds(){
        PackageConfiguration packageConfigs = new PackageConfiguration();
        packageConfigs.add(new Property(MavenPackageConfig.GROUP_ID, "com.tw"));
        packageConfigs.add(new Property(MavenPackageConfig.ARTIFACT_ID, "mypkg"));
        packageConfigs.add(new Property(MavenPackageConfig.POLL_VERSION_FROM, "1.2"));
        packageConfigs.add(new Property(MavenPackageConfig.POLL_VERSION_TO, "1.2"));
        MavenPackageConfig mavenPackageConfig = new MavenPackageConfig(packageConfigs);
        ValidationResult errors = new ValidationResult();
        mavenPackageConfig.validate(errors);
        assertFalse(errors.isSuccessful());
        ValidationError error = errors.getErrors().get(0);
        assertThat(error.getMessage(), is(MavenPackageConfig.INVALID_BOUNDS_MESSAGE));
    }

}