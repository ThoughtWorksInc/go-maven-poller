package com.tw.go.plugin.maven.config;

import com.thoughtworks.go.plugin.api.material.packagerepository.Property;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.tw.go.plugin.util.RepoUrl;

public class MavenRepoConfig {
    private final RepositoryConfiguration repoConfigs;
    private final Property repoUrlConfig;

    public MavenRepoConfig(RepositoryConfiguration repoConfigs) {
        this.repoConfigs = repoConfigs;
        repoUrlConfig = repoConfigs.get(RepoUrl.REPO_URL);
    }

    public String stringValueOf(Property packageConfiguration) {
        if (packageConfiguration == null) return null;
        return packageConfiguration.getValue();
    }

    public RepoUrl getRepoUrl() {
        return RepoUrl.create(
                withTrailingSlash(repoUrlConfig.getValue()),
                stringValueOf(repoConfigs.get(RepoUrl.USERNAME)),
                stringValueOf(repoConfigs.get(RepoUrl.PASSWORD)));
    }

    private String withTrailingSlash(String repoUrl) {
        if(repoUrl.endsWith("/")) return repoUrl;
        return repoUrl + "/";
    }

    public boolean isRepoUrlMissing() {
        return repoUrlConfig == null || repoUrlConfig.getValue() == null;
    }

    public static String[] getValidKeys() {
        return new String[]{RepoUrl.REPO_URL, RepoUrl.USERNAME, RepoUrl.PASSWORD};
    }
}
