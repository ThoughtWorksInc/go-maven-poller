package com.tw.go.plugin.maven.apimpl;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfigurations;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.validation.Errors;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.tw.go.plugin.maven.config.MavenPackageConfig;
import com.tw.go.plugin.maven.config.MavenRepoConfig;
import com.tw.go.plugin.util.RepoUrl;

import java.util.Arrays;

import static com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration.*;
import static com.tw.go.plugin.maven.config.MavenPackageConfig.*;

public class PluginConfig implements PackageRepositoryConfiguration {

    private static Logger LOGGER = Logger.getLoggerFor(PluginConfig.class);
    public static final PackageConfiguration REPO_CONFIG_REPO_URL =
            new PackageConfiguration(RepoUrl.REPO_URL).with(DISPLAY_NAME, "Maven Repo base URL").with(DISPLAY_ORDER, 0);

    public static final PackageConfiguration REPO_CONFIG_USERNAME =
            new PackageConfiguration(RepoUrl.USERNAME).with(REQUIRED, false).with(DISPLAY_NAME, "UserName").with(DISPLAY_ORDER, 1).with(PART_OF_IDENTITY, false);

    public static final PackageConfiguration REPO_CONFIG_PASSWORD =
            new PackageConfiguration(RepoUrl.PASSWORD).with(REQUIRED, false).with(SECURE, true).with(DISPLAY_NAME, "Password").with(DISPLAY_ORDER, 2).with(PART_OF_IDENTITY, false);

    public static final PackageConfiguration PKG_CONFIG_GROUP_ID =
            new PackageConfiguration(GROUP_ID).with(DISPLAY_NAME, "Group Id").with(DISPLAY_ORDER, 0);

    public static final PackageConfiguration PKG_CONFIG_ARTIFACT_ID =
            new PackageConfiguration(ARTIFACT_ID).with(DISPLAY_NAME, "Artifact Id").with(DISPLAY_ORDER, 1);

    public static final PackageConfiguration PKG_CONFIG_ARTIFACT_EXTN =
            new PackageConfiguration(ARTIFACT_EXTN).with(DISPLAY_NAME, "Artifact Extension (jar,war,ear...)").with(DISPLAY_ORDER, 2).with(PART_OF_IDENTITY, true);

    public static final PackageConfiguration PKG_CONFIG_POLL_VERSION_FROM =
            new PackageConfiguration(POLL_VERSION_FROM).with(REQUIRED, false).with(DISPLAY_NAME, "Version to poll >=").with(DISPLAY_ORDER, 3).with(PART_OF_IDENTITY, true);

    public static final PackageConfiguration PKG_CONFIG_POLL_VERSION_TO =
            new PackageConfiguration(POLL_VERSION_TO).with(REQUIRED, false).with(DISPLAY_NAME, "Version to poll <").with(DISPLAY_ORDER, 4).with(PART_OF_IDENTITY, true);


    public PackageConfigurations getRepositoryConfiguration() {
        PackageConfigurations configurations = new PackageConfigurations();
        configurations.addConfiguration(REPO_CONFIG_REPO_URL);
        configurations.addConfiguration(REPO_CONFIG_USERNAME);
        configurations.addConfiguration(REPO_CONFIG_PASSWORD);
        return configurations;
    }

    public PackageConfigurations getPackageConfiguration() {
        PackageConfigurations configurations = new PackageConfigurations();
        configurations.addConfiguration(PKG_CONFIG_GROUP_ID);
        configurations.addConfiguration(PKG_CONFIG_ARTIFACT_ID);
        configurations.addConfiguration(PKG_CONFIG_ARTIFACT_EXTN);
        configurations.addConfiguration(PKG_CONFIG_POLL_VERSION_FROM);
        configurations.addConfiguration(PKG_CONFIG_POLL_VERSION_TO);
        return configurations;
    }

    public boolean isRepositoryConfigurationValid(PackageConfigurations repoConfigs, Errors errors) {
        MavenRepoConfig mavenRepoConfig = new MavenRepoConfig(repoConfigs);
        if (mavenRepoConfig.isRepoUrlMissing()) {
            String message = "Repository url not specified";
            LOGGER.error(message);
            errors.addError(new ValidationError(RepoUrl.REPO_URL, message));
            return false;
        }
        mavenRepoConfig.getRepoUrl().validate(errors);
        detectInvalidKeys(repoConfigs, errors, MavenRepoConfig.getValidKeys());
        return !errors.hasErrors();
    }

    private void detectInvalidKeys(PackageConfigurations configs, Errors errors, String[] validKeys) {
        for (PackageConfiguration config : configs.list()) {
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

    public boolean isPackageConfigurationValid(PackageConfigurations packageConfig, PackageConfigurations repoConfig, Errors errors) {
        MavenPackageConfig mavenPackageConfig = new MavenPackageConfig(packageConfig);
        mavenPackageConfig.validate(errors);
        detectInvalidKeys(packageConfig, errors, MavenPackageConfig.getValidKeys());
        return !errors.hasErrors();
    }


}
