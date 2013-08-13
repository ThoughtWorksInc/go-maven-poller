package com.tw.go.plugin.maven.apimpl;

import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRepositoryMaterial;

@Extension
public class MaterialImpl implements PackageRepositoryMaterial {

    public PluginConfig getConfig() {
        return new PluginConfig();
    }

    public PollerImpl getPoller() {
        return new PollerImpl();
    }
}
