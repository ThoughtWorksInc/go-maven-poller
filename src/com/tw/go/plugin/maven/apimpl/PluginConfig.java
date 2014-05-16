package com.tw.go.plugin.maven.apimpl;

import com.thoughtworks.go.plugin.api.config.Configuration;
import com.thoughtworks.go.plugin.api.config.Property;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialProperty;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.tw.go.plugin.maven.config.MavenPackageConfig;
import com.tw.go.plugin.maven.config.MavenRepoConfig;
import com.tw.go.plugin.util.HttpRepoURL;
import com.tw.go.plugin.util.RepoUrl;

import java.util.Arrays;

import static com.thoughtworks.go.plugin.api.config.Property.*;
import static com.tw.go.plugin.maven.config.MavenPackageConfig.*;

public class PluginConfig implements PackageMaterialConfiguration {

    private static Logger LOGGER = Logger.getLoggerFor(PluginConfig.class);
    public static final Property REPO_CONFIG_REPO_URL =
            new PackageMaterialProperty(RepoUrl.REPO_URL).with(DISPLAY_NAME, "Maven Repo base URL").with(DISPLAY_ORDER, 0);

    public static final Property REPO_CONFIG_USERNAME =
            new PackageMaterialProperty(RepoUrl.USERNAME).with(REQUIRED, false).with(DISPLAY_NAME, "UserName").with(DISPLAY_ORDER, 1).with(PART_OF_IDENTITY, false);

    public static final Property REPO_CONFIG_PASSWORD =
            new PackageMaterialProperty(RepoUrl.PASSWORD).with(REQUIRED, false).with(SECURE, true).with(DISPLAY_NAME, "Password").with(DISPLAY_ORDER, 2).with(PART_OF_IDENTITY, false);

    public static final Property PKG_CONFIG_GROUP_ID =
            new PackageMaterialProperty(GROUP_ID).with(DISPLAY_NAME, "Group Id").with(DISPLAY_ORDER, 0);

    public static final Property PKG_CONFIG_ARTIFACT_ID =
            new PackageMaterialProperty(ARTIFACT_ID).with(DISPLAY_NAME, "Artifact Id").with(DISPLAY_ORDER, 1);

    public static final Property PKG_CONFIG_PACKAGING =
            new PackageMaterialProperty(PACKAGING).with(DISPLAY_NAME, "Packaging (jar,war,ear...)").with(DISPLAY_ORDER, 2).with(PART_OF_IDENTITY, true);

    public static final Property PKG_CONFIG_POLL_VERSION_FROM =
            new PackageMaterialProperty(POLL_VERSION_FROM).with(REQUIRED, false).with(DISPLAY_NAME, "Version to poll >=").with(DISPLAY_ORDER, 3).with(PART_OF_IDENTITY, true);

    public static final Property PKG_CONFIG_POLL_VERSION_TO =
            new PackageMaterialProperty(POLL_VERSION_TO).with(REQUIRED, false).with(DISPLAY_NAME, "Version to poll <").with(DISPLAY_ORDER, 4).with(PART_OF_IDENTITY, true);


    public RepositoryConfiguration getRepositoryConfiguration() {
        RepositoryConfiguration repoConfig = new RepositoryConfiguration();
        repoConfig.add(REPO_CONFIG_REPO_URL);
        repoConfig.add(REPO_CONFIG_USERNAME);
        repoConfig.add(REPO_CONFIG_PASSWORD);
        return repoConfig;
    }

    public PackageConfiguration getPackageConfiguration() {
        PackageConfiguration packageConfig = new PackageConfiguration();
        packageConfig.add(PKG_CONFIG_GROUP_ID);
        packageConfig.add(PKG_CONFIG_ARTIFACT_ID);
        packageConfig.add(PKG_CONFIG_PACKAGING);
        packageConfig.add(PKG_CONFIG_POLL_VERSION_FROM);
        packageConfig.add(PKG_CONFIG_POLL_VERSION_TO);
        return packageConfig;
    }

    @Override
    public ValidationResult isRepositoryConfigurationValid(RepositoryConfiguration repoConfig) {
        MavenRepoConfig mavenRepoConfig = new MavenRepoConfig(repoConfig);
        ValidationResult validationResult = new ValidationResult();
        if (mavenRepoConfig.isRepoUrlMissing()) {
            String message = "Repository url not specified";
            LOGGER.error(message);
            validationResult.addError(new ValidationError(RepoUrl.REPO_URL, message));
            return validationResult;
        }
        HttpRepoURL repoUrl = null;
        try {
            repoUrl = mavenRepoConfig.getRepoUrl();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            validationResult.addError(new ValidationError(RepoUrl.REPO_URL, e.getMessage()));
        }
        if (repoUrl != null)
            repoUrl.validate(validationResult);
        detectInvalidKeys(repoConfig, validationResult, MavenRepoConfig.getValidKeys());
        return validationResult;
    }

    private void detectInvalidKeys(Configuration config, ValidationResult errors, String[] validKeys) {
        for (Property property : config.list()) {
            boolean valid = false;
            for (String validKey : validKeys) {
                if (validKey.equals(property.getKey())) {
                    valid = true;
                    break;
                }
            }
            if (!valid)
                errors.addError(new ValidationError(String.format("Unsupported key: %s. Valid keys: %s", property.getKey(), Arrays.toString(validKeys))));
        }
    }

    public ValidationResult isPackageConfigurationValid(PackageConfiguration packageConfig, RepositoryConfiguration repoConfig) {
        ValidationResult validationResult = new ValidationResult();
        MavenPackageConfig mavenPackageConfig = new MavenPackageConfig(packageConfig);
        mavenPackageConfig.validate(validationResult);
        detectInvalidKeys(packageConfig, validationResult, MavenPackageConfig.getValidKeys());
        return validationResult;
    }

}
