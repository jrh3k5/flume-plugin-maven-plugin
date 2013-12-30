package com.github.jrh3k5.flume.mojo.plugin.artifact;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

import com.github.jrh3k5.flume.mojo.plugin.FlumePluginDependency;

public class FlumePluginDependencyArtifactFilter implements ArtifactFilter {
    private final FlumePluginDependency dependency;

    public FlumePluginDependencyArtifactFilter(FlumePluginDependency dependency) {
        this.dependency = dependency;
    }

    @Override
    public boolean include(Artifact artifact) {
        boolean matches = dependency.getGroupId().matches(artifact.getGroupId());
        matches &= dependency.getArtifactId().matches(artifact.getArtifactId());
        matches &= dependency.getVersion().matches(artifact.getBaseVersion());
        return matches;
    }

}
