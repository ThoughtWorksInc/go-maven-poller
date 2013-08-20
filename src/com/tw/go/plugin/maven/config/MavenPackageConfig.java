package com.tw.go.plugin.maven.config;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfigurations;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.thoughtworks.go.plugin.api.response.validation.Errors;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.tw.go.plugin.maven.LookupParams;
import com.tw.go.plugin.maven.client.Version;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class MavenPackageConfig {
    private static final Logger LOGGER = Logger.getLoggerFor(MavenPackageConfig.class);
    public static final String GROUP_ID = "GROUP_ID";
    public static final String ARTIFACT_ID = "ARTIFACT_ID";
    public static final String ARTIFACT_EXTN = "ARTIFACT_EXTN";
    public static final String POLL_VERSION_FROM = "POLL_VERSION_FROM";
    public static final String POLL_VERSION_TO = "POLL_VERSION_TO";
    public static final String INCLUDE_SNAPSHOTS = "INCLUDE_SNAPSHOTS";
    public static final String INVALID_BOUNDS_MESSAGE = "Lower Bound cannot be >= Upper Bound";
    private final PackageConfigurations packageConfigs;
    private final PackageConfiguration groupIdConfig;
    private final PackageConfiguration artifactIdConfig;

    public MavenPackageConfig(PackageConfigurations packageConfigs) {
        this.packageConfigs = packageConfigs;
        this.groupIdConfig = packageConfigs.get(GROUP_ID);
        this.artifactIdConfig = packageConfigs.get(ARTIFACT_ID);
    }

    public boolean isGroupIdMissing() {
        return groupIdConfig == null;
    }

    public String getGroupId() {
        return groupIdConfig.getValue();
    }
    public boolean isArtifactIdMissing() {
        return artifactIdConfig == null;
    }

    public String getArtifactId() {
        return artifactIdConfig.getValue();
    }

    public static String[] getValidKeys() {
        return new String[]{GROUP_ID, ARTIFACT_ID,ARTIFACT_EXTN, POLL_VERSION_FROM, POLL_VERSION_TO, INCLUDE_SNAPSHOTS};
    }

    public String getPollVersionFrom() {
        PackageConfiguration from = packageConfigs.get(POLL_VERSION_FROM);
        return (from == null) ? null : from.getValue();
    }

    public String getPollVersionTo() {
        PackageConfiguration to = packageConfigs.get(POLL_VERSION_TO);
        return (to == null) ? null : to.getValue();
    }

    public boolean isIncludeSnapshots() {
        PackageConfiguration config = packageConfigs.get(INCLUDE_SNAPSHOTS);
        if(config == null) return true;
        if(config.getValue() == null) return true;
        return !config.getValue().equalsIgnoreCase("no");
    }

    public boolean hasBounds() {
        return getPollVersionFrom() != null || getPollVersionTo() != null;
    }

    public String getArtifactExtn() {
        PackageConfiguration extn = packageConfigs.get(ARTIFACT_EXTN);
        return (extn == null) ? null : extn.getValue();
    }

    public LookupParams getLookupParams(PackageConfigurations repoConfig, PackageRevision previouslyKnownRevision) {
        return new LookupParams(
                new MavenRepoConfig(repoConfig).getRepoUrl(),
                getGroupId(), getArtifactId(), getArtifactExtn(),
                getPollVersionFrom(),
                getPollVersionTo(),
                previouslyKnownRevision, isIncludeSnapshots());
    }
    private void validateId(Errors errors, PackageConfiguration groupOrArtifactConfig, String what) {
        if(groupOrArtifactConfig == null || groupOrArtifactConfig.getValue() == null || isBlank(groupOrArtifactConfig.getValue().trim())){
            String message = what + " is not specified";
            LOGGER.info(message);
            errors.addError(new ValidationError(what, message));
            return;
        }
        String groupOrArtifactId = groupOrArtifactConfig.getValue();
        if ((groupOrArtifactId.contains("*") || groupOrArtifactId.contains("?"))) {
            String message = String.format("%s [%s] is invalid", what, groupOrArtifactId);
            LOGGER.info(message);
            errors.addError(new ValidationError(what, message));
        }
    }

    public void validate(Errors errors) {
        validateId(errors, groupIdConfig, GROUP_ID);
        validateId(errors, artifactIdConfig, ARTIFACT_ID);
        boolean lowerBoundSpecified = false;
        PackageConfiguration lowerBoundConfig = packageConfigs.get(POLL_VERSION_FROM);
        if(lowerBoundConfig != null && lowerBoundConfig.getValue() != null){
            lowerBoundSpecified = true;
            try{
                new Version(lowerBoundConfig.getValue());
            }catch (IllegalArgumentException ex){
                errors.addError(new ValidationError(POLL_VERSION_FROM, ex.getMessage()));
            }
        }
        boolean upperBoundSpecified = false;
        PackageConfiguration upperBoundConfig = packageConfigs.get(POLL_VERSION_TO);
        if(upperBoundConfig != null && upperBoundConfig.getValue() != null){
            upperBoundSpecified = true;
            try{
                new Version(upperBoundConfig.getValue());
            }catch (IllegalArgumentException ex){
                errors.addError(new ValidationError(POLL_VERSION_TO, ex.getMessage()));
            }
        }
        if(upperBoundSpecified && lowerBoundSpecified &&
                new Version(lowerBoundConfig.getValue()).greaterOrEqual(new Version(upperBoundConfig.getValue()))){
            errors.addError(new ValidationError(POLL_VERSION_FROM, INVALID_BOUNDS_MESSAGE));
        }
    }
}
