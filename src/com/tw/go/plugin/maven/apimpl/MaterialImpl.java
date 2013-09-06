package com.tw.go.plugin.maven.apimpl;

import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialPoller;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialProvider;

@Extension
public class MaterialImpl implements PackageMaterialProvider {

    public PackageMaterialConfiguration getConfig() {
        return new PluginConfig();
    }

    public PackageMaterialPoller getPoller() {
        return new PollerImpl();
    }
}
