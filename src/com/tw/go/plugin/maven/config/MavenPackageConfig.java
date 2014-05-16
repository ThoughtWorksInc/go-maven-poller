package com.tw.go.plugin.maven.config;

import com.thoughtworks.go.plugin.api.config.Property;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.tw.go.plugin.util.StringUtil;
import maven.Version;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class MavenPackageConfig {
    private static final Logger LOGGER = Logger.getLoggerFor(MavenPackageConfig.class);
    public static final String GROUP_ID = "GROUP_ID";
    public static final String ARTIFACT_ID = "ARTIFACT_ID";
    public static final String PACKAGING = "PACKAGING";
    public static final String POLL_VERSION_FROM = "POLL_VERSION_FROM";
    public static final String POLL_VERSION_TO = "POLL_VERSION_TO";
    public static final String INVALID_BOUNDS_MESSAGE = "Lower Bound cannot be >= Upper Bound";
    private final PackageConfiguration packageConfig;
    private final Property groupIdConfig;
    private final Property artifactIdConfig;

    public MavenPackageConfig(PackageConfiguration packageConfig) {
        this.packageConfig = packageConfig;
        this.groupIdConfig = packageConfig.get(GROUP_ID);
        this.artifactIdConfig = packageConfig.get(ARTIFACT_ID);
    }

    public String getGroupId() {
        return groupIdConfig.getValue();
    }

    public String getArtifactId() {
        return artifactIdConfig.getValue();
    }

    public static String[] getValidKeys() {
        return new String[]{GROUP_ID, ARTIFACT_ID, PACKAGING, POLL_VERSION_FROM, POLL_VERSION_TO};
    }

    public String getPollVersionFrom() {
        Property from = packageConfig.get(POLL_VERSION_FROM);
        return (from == null) ? null : from.getValue();
    }

    public String getPollVersionTo() {
        Property to = packageConfig.get(POLL_VERSION_TO);
        return (to == null) ? null : to.getValue();
    }

    public String getPackaging() {
        Property extn = packageConfig.get(PACKAGING);
        return (extn == null) ? null : extn.getValue();
    }

    public LookupParams getLookupParams(RepositoryConfiguration repoConfig, PackageRevision previouslyKnownRevision) {
        return new LookupParams(
                new MavenRepoConfig(repoConfig).getRepoUrl(),
                getGroupId(), getArtifactId(), getPackaging(),
                getPollVersionFrom(),
                getPollVersionTo(),
                previouslyKnownRevision);
    }

    private void validateId(ValidationResult errors, Property groupOrArtifactConfig, String what) {
        if (groupOrArtifactConfig == null || groupOrArtifactConfig.getValue() == null || isBlank(groupOrArtifactConfig.getValue().trim())) {
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

    public void validate(ValidationResult errors) {
        validateId(errors, groupIdConfig, GROUP_ID);
        validateId(errors, artifactIdConfig, ARTIFACT_ID);
        boolean lowerBoundSpecified = false;
        Property lowerBoundConfig = packageConfig.get(POLL_VERSION_FROM);
        if (lowerBoundConfig != null && lowerBoundConfig.getValue() != null && StringUtil.isNotBlank(lowerBoundConfig.getValue())) {
            lowerBoundSpecified = true;
            try {
                new Version(lowerBoundConfig.getValue());
            } catch (IllegalArgumentException ex) {
                errors.addError(new ValidationError(POLL_VERSION_FROM, ex.getMessage()));
            }
        }
        boolean upperBoundSpecified = false;
        Property upperBoundConfig = packageConfig.get(POLL_VERSION_TO);
        if (upperBoundConfig != null && upperBoundConfig.getValue() != null && StringUtil.isNotBlank(upperBoundConfig.getValue())) {
            upperBoundSpecified = true;
            try {
                new Version(upperBoundConfig.getValue());
            } catch (IllegalArgumentException ex) {
                errors.addError(new ValidationError(POLL_VERSION_TO, ex.getMessage()));
            }
        }
        if (upperBoundSpecified && lowerBoundSpecified &&
                new Version(lowerBoundConfig.getValue()).greaterOrEqual(new Version(upperBoundConfig.getValue()))) {
            errors.addError(new ValidationError(POLL_VERSION_FROM, INVALID_BOUNDS_MESSAGE));
        }
    }
}
