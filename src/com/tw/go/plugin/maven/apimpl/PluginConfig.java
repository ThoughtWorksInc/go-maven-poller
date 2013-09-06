package com.tw.go.plugin.maven.apimpl;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.*;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.tw.go.plugin.maven.config.MavenPackageConfig;
import com.tw.go.plugin.maven.config.MavenRepoConfig;
import com.tw.go.plugin.util.RepoUrl;

import java.util.Arrays;

import static com.thoughtworks.go.plugin.api.material.packagerepository.Property.*;
import static com.tw.go.plugin.maven.config.MavenPackageConfig.*;

public class PluginConfig implements PackageMaterialConfiguration {

    private static Logger LOGGER = Logger.getLoggerFor(PluginConfig.class);
    public static final Property REPO_CONFIG_REPO_URL =
            new Property(RepoUrl.REPO_URL).with(DISPLAY_NAME, "Maven Repo base URL").with(DISPLAY_ORDER, 0);

    public static final Property REPO_CONFIG_USERNAME =
            new Property(RepoUrl.USERNAME).with(REQUIRED, false).with(DISPLAY_NAME, "UserName").with(DISPLAY_ORDER, 1).with(PART_OF_IDENTITY, false);

    public static final Property REPO_CONFIG_PASSWORD =
            new Property(RepoUrl.PASSWORD).with(REQUIRED, false).with(SECURE, true).with(DISPLAY_NAME, "Password").with(DISPLAY_ORDER, 2).with(PART_OF_IDENTITY, false);

    public static final Property PKG_CONFIG_GROUP_ID =
            new Property(GROUP_ID).with(DISPLAY_NAME, "Group Id").with(DISPLAY_ORDER, 0);

    public static final Property PKG_CONFIG_ARTIFACT_ID =
            new Property(ARTIFACT_ID).with(DISPLAY_NAME, "Artifact Id").with(DISPLAY_ORDER, 1);

    public static final Property PKG_CONFIG_ARTIFACT_EXTN =
            new Property(ARTIFACT_EXTN).with(DISPLAY_NAME, "Artifact Extension (jar,war,ear...)").with(DISPLAY_ORDER, 2).with(PART_OF_IDENTITY, true);

    public static final Property PKG_CONFIG_POLL_VERSION_FROM =
            new Property(POLL_VERSION_FROM).with(REQUIRED, false).with(DISPLAY_NAME, "Version to poll >=").with(DISPLAY_ORDER, 3).with(PART_OF_IDENTITY, true);

    public static final Property PKG_CONFIG_POLL_VERSION_TO =
            new Property(POLL_VERSION_TO).with(REQUIRED, false).with(DISPLAY_NAME, "Version to poll <").with(DISPLAY_ORDER, 4).with(PART_OF_IDENTITY, true);


    public RepositoryConfiguration getRepositoryConfiguration() {
        RepositoryConfiguration configurations = new RepositoryConfiguration();
        configurations.addConfiguration(REPO_CONFIG_REPO_URL);
        configurations.addConfiguration(REPO_CONFIG_USERNAME);
        configurations.addConfiguration(REPO_CONFIG_PASSWORD);
        return configurations;
    }

    public PackageConfiguration getPackageConfiguration() {
        PackageConfiguration configurations = new PackageConfiguration();
        configurations.addConfiguration(PKG_CONFIG_GROUP_ID);
        configurations.addConfiguration(PKG_CONFIG_ARTIFACT_ID);
        configurations.addConfiguration(PKG_CONFIG_ARTIFACT_EXTN);
        configurations.addConfiguration(PKG_CONFIG_POLL_VERSION_FROM);
        configurations.addConfiguration(PKG_CONFIG_POLL_VERSION_TO);
        return configurations;
    }

    @Override
    public ValidationResult isRepositoryConfigurationValid(RepositoryConfiguration repoConfigs) {
        MavenRepoConfig mavenRepoConfig = new MavenRepoConfig(repoConfigs);
        ValidationResult validationResult = new ValidationResult();
        if (mavenRepoConfig.isRepoUrlMissing()) {
            String message = "Repository url not specified";
            LOGGER.error(message);
            validationResult.addError(new ValidationError(RepoUrl.REPO_URL, message));
            return validationResult;
        }
        mavenRepoConfig.getRepoUrl().validate(validationResult);
        detectInvalidKeys(repoConfigs, validationResult, MavenRepoConfig.getValidKeys());
        return validationResult;
    }

    private void detectInvalidKeys(Configuration configs, ValidationResult errors, String[] validKeys) {
        for (Property config : configs.list()) {
            boolean valid = false;
            for (String validKey : validKeys) {
                if (validKey.equals(config.getKey())) {
                    valid = true;
                    break;
                }
            }
            if (!valid)
                errors.addError(new ValidationError(String.format("Unsupported key: %s. Valid keys: %s", config.getKey(), Arrays.toString(validKeys))));
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
