package com.github.jrh3k5.flume.mojo.plugin.artifact;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

import com.github.jrh3k5.flume.mojo.plugin.FlumePluginDependency;

/**
 * An {@link ArtifactFilter} that only qualifies artifacts that match the given {@link FlumePluginDependency}.
 * 
 * @author Joshua Hyde
 */

public class FlumePluginDependencyArtifactFilter implements ArtifactFilter {
    private final FlumePluginDependency dependency;

    /**
     * Create a filter.
     * 
     * @param dependency
     *            The {@link FlumePluginDependency} to drive matching of an artifact.
     */
    public FlumePluginDependencyArtifactFilter(FlumePluginDependency dependency) {
        this.dependency = dependency;
    }

    @Override
    public boolean include(Artifact artifact) {
        boolean matches = dependency.getGroupId().matches(artifact.getGroupId());
        matches &= dependency.getArtifactId().matches(artifact.getArtifactId());
        return matches;
    }

}
