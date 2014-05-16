package com.tw.go.plugin.maven.config;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialProperty;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class MavenPackageConfigTest {
    @Test
    public void shouldValidateBounds(){
        PackageConfiguration packageConfigs = new PackageConfiguration();
        packageConfigs.add(new PackageMaterialProperty(MavenPackageConfig.GROUP_ID, "com.tw"));
        packageConfigs.add(new PackageMaterialProperty(MavenPackageConfig.ARTIFACT_ID, "mypkg"));
        packageConfigs.add(new PackageMaterialProperty(MavenPackageConfig.POLL_VERSION_FROM, "1.2"));
        packageConfigs.add(new PackageMaterialProperty(MavenPackageConfig.POLL_VERSION_TO, "1.5"));
        MavenPackageConfig mavenPackageConfig = new MavenPackageConfig(packageConfigs);
        ValidationResult errors = new ValidationResult();
        mavenPackageConfig.validate(errors);
        assertTrue(errors.isSuccessful());
    }
    @Test
    public void shouldRejectInvalidBounds(){
        PackageConfiguration packageConfigs = new PackageConfiguration();
        packageConfigs.add(new PackageMaterialProperty(MavenPackageConfig.GROUP_ID, "com.tw"));
        packageConfigs.add(new PackageMaterialProperty(MavenPackageConfig.ARTIFACT_ID, "mypkg"));
        packageConfigs.add(new PackageMaterialProperty(MavenPackageConfig.POLL_VERSION_FROM, "1.2"));
        packageConfigs.add(new PackageMaterialProperty(MavenPackageConfig.POLL_VERSION_TO, "1.1"));
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
        packageConfigs.add(new PackageMaterialProperty(MavenPackageConfig.GROUP_ID, "com.tw"));
        packageConfigs.add(new PackageMaterialProperty(MavenPackageConfig.ARTIFACT_ID, "mypkg"));
        packageConfigs.add(new PackageMaterialProperty(MavenPackageConfig.POLL_VERSION_FROM, "1.2"));
        packageConfigs.add(new PackageMaterialProperty(MavenPackageConfig.POLL_VERSION_TO, "1.2"));
        MavenPackageConfig mavenPackageConfig = new MavenPackageConfig(packageConfigs);
        ValidationResult errors = new ValidationResult();
        mavenPackageConfig.validate(errors);
        assertFalse(errors.isSuccessful());
        ValidationError error = errors.getErrors().get(0);
        assertThat(error.getMessage(), is(MavenPackageConfig.INVALID_BOUNDS_MESSAGE));
    }

}