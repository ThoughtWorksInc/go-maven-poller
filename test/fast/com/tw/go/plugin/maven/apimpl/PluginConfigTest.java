package com.tw.go.plugin.maven.apimpl;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.Property;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.tw.go.plugin.maven.config.MavenPackageConfig;
import com.tw.go.plugin.maven.config.MavenRepoConfig;
import com.tw.go.plugin.util.InvalidRepoUrl;
import com.tw.go.plugin.util.RepoUrl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.thoughtworks.go.plugin.api.material.packagerepository.Property.*;
import static com.tw.go.plugin.maven.config.MavenPackageConfig.*;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class PluginConfigTest {
    private PluginConfig pluginConfig;
    private final ValidationError noArtifact = new ValidationError(ARTIFACT_ID, "ARTIFACT_ID is not specified");
    private final ValidationError noGroup = new ValidationError(GROUP_ID, "GROUP_ID is not specified");

    @Before
    public void setUp() {
        pluginConfig = new PluginConfig();
    }

    @Test
    public void shouldGetRepositoryConfiguration() {
        RepositoryConfiguration configurations = pluginConfig.getRepositoryConfiguration();
        assertThat(configurations.get(RepoUrl.REPO_URL), is(notNullValue()));
        assertThat(configurations.get(RepoUrl.REPO_URL).getOption(SECURE), is(false));
        assertThat(configurations.get(RepoUrl.REPO_URL).getOption(Property.REQUIRED), is(true));
        assertThat(configurations.get(RepoUrl.REPO_URL).getOption(DISPLAY_NAME), is("Maven Repo base URL"));
        assertThat(configurations.get(RepoUrl.REPO_URL).getOption(DISPLAY_ORDER), is(0));
        assertThat(configurations.get(RepoUrl.USERNAME), is(notNullValue()));
        assertThat(configurations.get(RepoUrl.USERNAME).getOption(SECURE), is(false));
        assertThat(configurations.get(RepoUrl.USERNAME).getOption(REQUIRED), is(false));
        assertThat(configurations.get(RepoUrl.USERNAME).getOption(DISPLAY_NAME), is("UserName"));
        assertThat(configurations.get(RepoUrl.USERNAME).getOption(DISPLAY_ORDER), is(1));
        assertThat(configurations.get(RepoUrl.PASSWORD), is(notNullValue()));
        assertThat(configurations.get(RepoUrl.PASSWORD).getOption(SECURE), is(true));
        assertThat(configurations.get(RepoUrl.PASSWORD).getOption(REQUIRED), is(false));
        assertThat(configurations.get(RepoUrl.PASSWORD).getOption(DISPLAY_NAME), is("Password"));
        assertThat(configurations.get(RepoUrl.PASSWORD).getOption(DISPLAY_ORDER), is(2));
    }

    @Test
    public void shouldGetPackageConfiguration() {
        PackageConfiguration configurations = pluginConfig.getPackageConfiguration();
        assertNotNull(configurations.get(GROUP_ID));
        assertThat(configurations.get(GROUP_ID).getOption(DISPLAY_NAME), is("Group Id"));
        assertThat(configurations.get(GROUP_ID).getOption(DISPLAY_ORDER), is(0));
        assertNotNull(configurations.get(POLL_VERSION_FROM));
        assertThat(configurations.get(POLL_VERSION_FROM).getOption(DISPLAY_NAME), is("Version to poll >="));
        assertThat(configurations.get(POLL_VERSION_FROM).getOption(DISPLAY_ORDER), is(3));
        assertThat(configurations.get(POLL_VERSION_FROM).getOption(REQUIRED), is(false));
        assertNotNull(configurations.get(POLL_VERSION_TO));
        assertThat(configurations.get(POLL_VERSION_TO).getOption(DISPLAY_NAME), is("Version to poll <"));
        assertThat(configurations.get(POLL_VERSION_TO).getOption(DISPLAY_ORDER), is(4));
        assertThat(configurations.get(POLL_VERSION_TO).getOption(REQUIRED), is(false));
    }

    @Test
    public void shouldValidateRepoUrl() {
        assertForRepositoryConfigurationErrors(new RepositoryConfiguration(), asList(new ValidationError(RepoUrl.REPO_URL, "Repository url not specified")), false);
        assertForRepositoryConfigurationErrors(configurations(RepoUrl.REPO_URL, null), asList(new ValidationError(RepoUrl.REPO_URL, InvalidRepoUrl.MESSAGE)), false);
        assertForRepositoryConfigurationErrors(configurations(RepoUrl.REPO_URL, ""), asList(new ValidationError(RepoUrl.REPO_URL, InvalidRepoUrl.MESSAGE)), false);
        assertForRepositoryConfigurationErrors(configurations(RepoUrl.REPO_URL, "incorrectUrl"), asList(new ValidationError(RepoUrl.REPO_URL, InvalidRepoUrl.MESSAGE)), false);
        assertForRepositoryConfigurationErrors(configurations(RepoUrl.REPO_URL, "http://correct.com/url"), new ArrayList<ValidationError>(), true);
    }
    @Test
    public void shouldRejectUnsupportedTagsInRepoConfig() {
        RepositoryConfiguration repoConfig = new RepositoryConfiguration();
        repoConfig.add(new Property(RepoUrl.REPO_URL, "http://maven.org"));
        repoConfig.add(new Property("unsupported_key", "value"));
        assertForRepositoryConfigurationErrors(
                repoConfig,
                asList(new ValidationError("Unsupported key: unsupported_key. Valid keys: "+ Arrays.toString(MavenRepoConfig.getValidKeys()))),
                false);

    }
    @Test
    public void shouldRejectUnsupportedTagsInPkgConfig() {
        PackageConfiguration pkgConfig = new PackageConfiguration();
        pkgConfig.add(new Property(GROUP_ID, "abc"));
        pkgConfig.add(new Property("unsupported_key", "value"));
        assertForPackageConfigurationErrors(
                pkgConfig,
                asList(noArtifact, new ValidationError("Unsupported key: unsupported_key. Valid keys: "+ Arrays.toString(MavenPackageConfig.getValidKeys()))),
                false);
    }

    @Test
    public void shouldValidatePackageId() {
        List<ValidationError> expectedErrors = asList(
                noGroup,
                noArtifact
        );
        assertForPackageConfigurationErrors(new PackageConfiguration(), expectedErrors, false);
        assertForPackageConfigurationErrors(packageConfiguration(GROUP_ID, null), expectedErrors, false);
        assertForPackageConfigurationErrors(packageConfiguration(GROUP_ID, ""), expectedErrors, false);
        assertForPackageConfigurationErrors(packageConfiguration(GROUP_ID, "go-age?nt-*"), asList(new ValidationError(GROUP_ID, "GROUP_ID [go-age?nt-*] is invalid"), noArtifact), false);
        assertForPackageConfigurationErrors(packageConfiguration(GROUP_ID, "go-agent"), asList(noArtifact), false);
    }

    private void assertForRepositoryConfigurationErrors(RepositoryConfiguration repositoryConfigurations, List<ValidationError> expectedErrors, boolean expectedValidationResult) {
        ValidationResult validationResult = pluginConfig.isRepositoryConfigurationValid(repositoryConfigurations);
        assertThat(validationResult.isSuccessful(), is(expectedValidationResult));
        assertThat(validationResult.getErrors().size(), is(expectedErrors.size()));
        assertThat(validationResult.getErrors().containsAll(expectedErrors), is(true));
    }

    private void assertForPackageConfigurationErrors(PackageConfiguration packageConfigurations, List<ValidationError> expectedErrors, boolean expectedValidationResult) {
        final RepositoryConfiguration repoConfig = new RepositoryConfiguration();
        repoConfig.add(new Property(RepoUrl.REPO_URL, "http://maven.org/v2"));
        ValidationResult result = pluginConfig.isPackageConfigurationValid(packageConfigurations, repoConfig);
        assertThat(result.isSuccessful(), is(expectedValidationResult));
        assertThat(result.getErrors().size(), is(expectedErrors.size()));
        assertThat(result.getErrors().containsAll(expectedErrors), is(true));
    }

    private RepositoryConfiguration configurations(String key, String value) {
        RepositoryConfiguration configurations = new RepositoryConfiguration();
        configurations.add(new Property(key, value));
        return configurations;
    }
    private PackageConfiguration packageConfiguration(String key, String value) {
        PackageConfiguration configurations = new PackageConfiguration();
        configurations.add(new Property(key, value));
        return configurations;
    }
}
