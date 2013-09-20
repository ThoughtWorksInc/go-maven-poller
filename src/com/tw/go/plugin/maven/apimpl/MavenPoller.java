package com.tw.go.plugin.maven.apimpl;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialPoller;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.Result;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.tw.go.plugin.maven.LookupParams;
import com.tw.go.plugin.maven.client.MavenRepositoryClient;
import com.tw.go.plugin.maven.client.RepositoryConnector;
import com.tw.go.plugin.maven.client.Version;
import com.tw.go.plugin.maven.config.MavenPackageConfig;
import com.tw.go.plugin.maven.config.MavenRepoConfig;
import com.tw.go.plugin.util.RepoUrl;

import static com.tw.go.plugin.maven.config.MavenPackageConfig.ARTIFACT_ID;
import static com.tw.go.plugin.maven.config.MavenPackageConfig.GROUP_ID;

public class MavenPoller implements PackageMaterialPoller {
    private static Logger LOGGER = Logger.getLoggerFor(MavenPoller.class);

    public PackageRevision getLatestRevision(PackageConfiguration packageConfig, RepositoryConfiguration repoConfig) {
        LOGGER.info(String.format("getLatestRevision called with groupId %s, artifactId %s, for repo: %s",
                packageConfig.get(GROUP_ID).getValue(),
                packageConfig.get(ARTIFACT_ID).getValue(),
                repoConfig.get(RepoUrl.REPO_URL).getValue()));
        validateConfig(repoConfig, packageConfig);
        LookupParams params = new MavenPackageConfig(packageConfig).getLookupParams(repoConfig, null);
        PackageRevision packageRevision = poll(params);
        if(packageRevision != null){
            LOGGER.info(String.format("getLatestRevision returning with %s, %s",
                packageRevision.getRevision(), packageRevision.getTimestamp()));
        }
        return packageRevision;
    }

    public PackageRevision latestModificationSince(PackageConfiguration packageConfig, RepositoryConfiguration repoConfig, PackageRevision previouslyKnownRevision) {
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
    public Result checkConnectionToRepository(RepositoryConfiguration repoConfig) {
        RepoUrl repoUrl = new MavenRepoConfig(repoConfig).getRepoUrl();
        Result result = new Result();
        try {
            if (!new RepositoryConnector().testConnection(repoUrl.getUrlStr(), repoUrl.getCredentials().getUser(), repoUrl.getCredentials().getPassword())) {
                result.withErrorMessages("Did not get HTTP Status 200 response");
            }
        } catch (Exception e) {
            result.withErrorMessages(e.getMessage());
        }
        LOGGER.info(result.getMessagesForDisplay());
        return result;
    }

    @Override
    public Result checkConnectionToPackage(PackageConfiguration packageConfig, RepositoryConfiguration repoConfig) {
        Result repoCheckResult = checkConnectionToRepository(repoConfig);
        if (!repoCheckResult.isSuccessful())
            return repoCheckResult;
        PackageRevision packageRevision = getLatestRevision(packageConfig, repoConfig);
        Result result = new Result();
        if (packageRevision != null) {
            result.withSuccessMessages("Found " + packageRevision.getRevision());
        } else {
            result.withErrorMessages("Could not find package");
        }
        return result;
    }

    private void validateConfig(RepositoryConfiguration repoConfig, PackageConfiguration packageConfig) {
        ValidationResult validationResult = new PluginConfig().isRepositoryConfigurationValid(repoConfig);
        validationResult.addErrors(new PluginConfig().isPackageConfigurationValid(packageConfig, repoConfig).getErrors());
        if (!validationResult.isSuccessful()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ValidationError validationError : validationResult.getErrors()) {
                stringBuilder.append(validationError.getMessage()).append("; ");
            }
            String errorString = stringBuilder.toString();
            throw new RuntimeException(errorString.substring(0, errorString.length() - 2));
        }
    }

    PackageRevision poll(LookupParams params) {
        Version latest = new MavenRepositoryClient(params).getLatest();
        if (latest == null) return null;
        return latest.toPackageRevision();
    }
}
