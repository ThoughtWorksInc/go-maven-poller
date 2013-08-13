package com.tw.go.plugin.maven.apimpl;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfigurations;
import com.thoughtworks.go.plugin.api.response.validation.Errors;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.tw.go.plugin.maven.config.MavenPackageConfig;
import com.tw.go.plugin.maven.config.MavenRepoConfig;
import com.tw.go.plugin.util.InvalidRepoUrl;
import com.tw.go.plugin.util.RepoUrl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration.*;
import static com.tw.go.plugin.maven.config.MavenPackageConfig.*;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class PluginConfigTest {
    private PluginConfig pluginConfig;

    @Before
    public void setUp() {
        pluginConfig = new PluginConfig();
    }

    @Test
    public void shouldGetRepositoryConfiguration() {
        PackageConfigurations configurations = pluginConfig.getRepositoryConfiguration();
        assertThat(configurations.get(RepoUrl.REPO_URL), is(notNullValue()));
        assertThat(configurations.get(RepoUrl.REPO_URL).getOption(PackageConfiguration.SECURE), is(false));
        assertThat(configurations.get(RepoUrl.REPO_URL).getOption(PackageConfiguration.REQUIRED), is(true));
        assertThat(configurations.get(RepoUrl.REPO_URL).getOption(DISPLAY_NAME), is("Package Source or Feed Server URL"));
        assertThat(configurations.get(RepoUrl.REPO_URL).getOption(DISPLAY_ORDER), is(0));
        assertThat(configurations.get(RepoUrl.USERNAME), is(notNullValue()));
        assertThat(configurations.get(RepoUrl.USERNAME).getOption(PackageConfiguration.SECURE), is(false));
        assertThat(configurations.get(RepoUrl.USERNAME).getOption(PackageConfiguration.REQUIRED), is(false));
        assertThat(configurations.get(RepoUrl.USERNAME).getOption(DISPLAY_NAME), is("UserName"));
        assertThat(configurations.get(RepoUrl.USERNAME).getOption(DISPLAY_ORDER), is(1));
        assertThat(configurations.get(RepoUrl.PASSWORD), is(notNullValue()));
        assertThat(configurations.get(RepoUrl.PASSWORD).getOption(PackageConfiguration.SECURE), is(true));
        assertThat(configurations.get(RepoUrl.PASSWORD).getOption(PackageConfiguration.REQUIRED), is(false));
        assertThat(configurations.get(RepoUrl.PASSWORD).getOption(DISPLAY_NAME), is("Password"));
        assertThat(configurations.get(RepoUrl.PASSWORD).getOption(DISPLAY_ORDER), is(2));
    }

    @Test
    public void shouldGetPackageConfiguration() {
        PackageConfigurations configurations = pluginConfig.getPackageConfiguration();
        assertNotNull(configurations.get(GROUP_ID));
        assertThat(configurations.get(GROUP_ID).getOption(DISPLAY_NAME), is("Package Id"));
        assertThat(configurations.get(GROUP_ID).getOption(DISPLAY_ORDER), is(0));
        assertNotNull(configurations.get(POLL_VERSION_FROM));
        assertThat(configurations.get(POLL_VERSION_FROM).getOption(DISPLAY_NAME), is("Version to poll >="));
        assertThat(configurations.get(POLL_VERSION_FROM).getOption(DISPLAY_ORDER), is(1));
        assertThat(configurations.get(POLL_VERSION_FROM).getOption(REQUIRED), is(false));
        assertNotNull(configurations.get(POLL_VERSION_TO));
        assertThat(configurations.get(POLL_VERSION_TO).getOption(DISPLAY_NAME), is("Version to poll <"));
        assertThat(configurations.get(POLL_VERSION_TO).getOption(DISPLAY_ORDER), is(2));
        assertThat(configurations.get(POLL_VERSION_TO).getOption(REQUIRED), is(false));
    }

    @Test
    public void shouldValidateRepoUrl() {
        assertForRepositoryConfigurationErrors(new PackageConfigurations(), asList(new ValidationError(RepoUrl.REPO_URL, "Repository url not specified")), false);
        assertForRepositoryConfigurationErrors(configurations(RepoUrl.REPO_URL, null), asList(new ValidationError(RepoUrl.REPO_URL, InvalidRepoUrl.MESSAGE)), false);
        assertForRepositoryConfigurationErrors(configurations(RepoUrl.REPO_URL, ""), asList(new ValidationError(RepoUrl.REPO_URL, InvalidRepoUrl.MESSAGE)), false);
        assertForRepositoryConfigurationErrors(configurations(RepoUrl.REPO_URL, "incorrectUrl"), asList(new ValidationError(RepoUrl.REPO_URL, InvalidRepoUrl.MESSAGE)), false);
        assertForRepositoryConfigurationErrors(configurations(RepoUrl.REPO_URL, "http://correct.com/url"), new ArrayList<ValidationError>(), true);
    }
    @Test
    public void shouldRejectUnsupportedTagsInRepoConfig() {
        PackageConfigurations repoConfig = new PackageConfigurations();
        repoConfig.add(new PackageConfiguration(RepoUrl.REPO_URL, "http://maven.org"));
        repoConfig.add(new PackageConfiguration("unsupported_key", "value"));
        assertForRepositoryConfigurationErrors(
                repoConfig,
                asList(new ValidationError("Unsupported key: unsupported_key. Valid keys: "+ Arrays.toString(MavenRepoConfig.getValidKeys()))),
                false);

    }
    @Test
    public void shouldRejectUnsupportedTagsInPkgConfig() {
        PackageConfigurations pkgConfig = new PackageConfigurations();
        pkgConfig.add(new PackageConfiguration(GROUP_ID, "abc"));
        pkgConfig.add(new PackageConfiguration("unsupported_key", "value"));
        assertForPackageConfigurationErrors(
                pkgConfig,
                asList(new ValidationError("Unsupported key: unsupported_key. Valid keys: "+ Arrays.toString(MavenPackageConfig.getValidKeys()))),
                false);
    }

    @Test
    public void shouldValidatePackageId() {
        assertForPackageConfigurationErrors(new PackageConfigurations(), asList(new ValidationError(GROUP_ID, "Package id not specified")), false);
        assertForPackageConfigurationErrors(configurations(GROUP_ID, null), asList(new ValidationError(GROUP_ID, "Package id is null")), false);
        assertForPackageConfigurationErrors(configurations(GROUP_ID, ""), asList(new ValidationError(GROUP_ID, "Package id is empty")), false);
        assertForPackageConfigurationErrors(configurations(GROUP_ID, "go-age?nt-*"), asList(new ValidationError(GROUP_ID, "Package id [go-age?nt-*] is invalid")), false);
        assertForPackageConfigurationErrors(configurations(GROUP_ID, "go-agent"), new ArrayList<ValidationError>(), true);
    }

    private void assertForRepositoryConfigurationErrors(PackageConfigurations repositoryConfigurations, List<ValidationError> expectedErrors, boolean expectedValidationResult) {
        Errors errors = new Errors();
        boolean result = pluginConfig.isRepositoryConfigurationValid(repositoryConfigurations, errors);
        assertThat(result, is(expectedValidationResult));
        assertThat(errors.getErrors().size(), is(expectedErrors.size()));
        assertThat(errors.getErrors().containsAll(expectedErrors), is(true));
    }

    private void assertForPackageConfigurationErrors(PackageConfigurations packageConfigurations, List<ValidationError> expectedErrors, boolean expectedValidationResult) {
        Errors errors = new Errors();
        final PackageConfigurations repoConfig = new PackageConfigurations();
        repoConfig.add(new PackageConfiguration(RepoUrl.REPO_URL, "http://maven.org/v2"));
        boolean result = pluginConfig.isPackageConfigurationValid(packageConfigurations, repoConfig, errors);
        assertThat(result, is(expectedValidationResult));
        assertThat(errors.getErrors().size(), is(expectedErrors.size()));
        assertThat(errors.getErrors().containsAll(expectedErrors), is(true));
    }

    private PackageConfigurations configurations(String key, String value) {
        PackageConfigurations packageConfigurations = new PackageConfigurations();
        PackageConfigurations configurations = packageConfigurations;
        configurations.add(new PackageConfiguration(key, value));
        return configurations;
    }
}
