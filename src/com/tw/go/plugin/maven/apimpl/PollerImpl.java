package com.tw.go.plugin.maven.apimpl;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfigurations;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRepositoryPoller;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.thoughtworks.go.plugin.api.response.OperationResponse;
import com.thoughtworks.go.plugin.api.response.validation.Errors;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.tw.go.plugin.maven.LookupParams;
import com.tw.go.plugin.maven.client.MavenRepositoryClient;
import com.tw.go.plugin.maven.client.RepositoryConnector;
import com.tw.go.plugin.maven.client.Version;
import com.tw.go.plugin.maven.config.MavenPackageConfig;
import com.tw.go.plugin.maven.config.MavenRepoConfig;
import com.tw.go.plugin.util.RepoUrl;

import static com.tw.go.plugin.maven.config.MavenPackageConfig.ARTIFACT_ID;
import static com.tw.go.plugin.maven.config.MavenPackageConfig.GROUP_ID;

public class PollerImpl implements PackageRepositoryPoller {
    private static Logger LOGGER = Logger.getLoggerFor(PollerImpl.class);

    public PackageRevision getLatestRevision(PackageConfigurations packageConfig, PackageConfigurations repoConfig) {
        LOGGER.info(String.format("getLatestRevision called with groupId %s, artifactId %s, for repo: %s",
                packageConfig.get(GROUP_ID).getValue(),
                packageConfig.get(ARTIFACT_ID).getValue(),
                repoConfig.get(RepoUrl.REPO_URL).getValue()));
        validateConfig(repoConfig, packageConfig);
        LookupParams params = new MavenPackageConfig(packageConfig).getLookupParams(repoConfig, null);
        PackageRevision packageRevision = poll(params);
        LOGGER.info(String.format("getLatestRevision returning with %s, %s",
                packageRevision.getRevision(), packageRevision.getTimestamp()));
        return packageRevision;
    }

    public PackageRevision latestModificationSince(PackageConfigurations packageConfig, PackageConfigurations repoConfig, PackageRevision previouslyKnownRevision) {
        LOGGER.info(String.format("latestModificationSince called with groupId %s, for repo: %s",
                packageConfig.get(GROUP_ID).getValue(), repoConfig.get(RepoUrl.REPO_URL).getValue()));
        validateConfig(repoConfig, packageConfig);
        LookupParams params = new MavenPackageConfig(packageConfig).getLookupParams(repoConfig, previouslyKnownRevision);
        PackageRevision updatedPackage = poll(params);
        if (updatedPackage == null) {
            LOGGER.info(String.format("no modification since %s", previouslyKnownRevision.getRevision()));
            return null;
        }
        LOGGER.info(String.format("latestModificationSince returning with %s, %s",
                updatedPackage.getRevision(), updatedPackage.getTimestamp()));
        if (updatedPackage.getTimestamp().getTime() < previouslyKnownRevision.getTimestamp().getTime())
            LOGGER.warn(String.format("Updated Package %s published earlier (%s) than previous (%s, %s)",
                    updatedPackage.getRevision(), updatedPackage.getTimestamp(), previouslyKnownRevision.getRevision(), previouslyKnownRevision.getTimestamp()));
        return updatedPackage;
    }

    @Override
    public OperationResponse canConnectToRepository(PackageConfigurations packageConfigurations) {
        RepoUrl repoUrl = new MavenRepoConfig(packageConfigurations).getRepoUrl();
        OperationResponse response = new OperationResponse();
        try {
            boolean result = new RepositoryConnector().testConnection(repoUrl.forDisplay(), repoUrl.getCredentials().getUser(), repoUrl.getCredentials().getPassword());
            if (result) {
                response.withSuccessMessages("Connection ok");
            } else {
                response.withErrorMessages("Connection failed");
            }
        } catch (Exception e) {
            response.withErrorMessages(e.getMessage());
        }
        return response;
    }

    private void validateConfig(PackageConfigurations repoConfig, PackageConfigurations packageConfig) {
        Errors errors = new Errors();
        new PluginConfig().isRepositoryConfigurationValid(repoConfig, errors);
        new PluginConfig().isPackageConfigurationValid(packageConfig, repoConfig, errors);
        if (errors.hasErrors()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ValidationError validationError : errors.getErrors()) {
                stringBuilder.append(validationError.getMessage()).append("; ");
            }
            String errorString = stringBuilder.toString();
            throw new RuntimeException(errorString.substring(0, errorString.length() - 2));
        }
    }

    PackageRevision poll(LookupParams params) {
        Version latest = new MavenRepositoryClient(params).getLatest();
        if(latest == null) return null;
        return latest.toPackageRevision();
    }
}
