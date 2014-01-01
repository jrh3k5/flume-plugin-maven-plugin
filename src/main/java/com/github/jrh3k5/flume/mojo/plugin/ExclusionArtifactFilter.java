package com.github.jrh3k5.flume.mojo.plugin;

import java.util.Collection;
import java.util.Collections;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

/**
 * An {@link ArtifactFilter} that excludes anything in the given collection of exclusions.
 * 
 * @author Joshua Hyde
 */

public class ExclusionArtifactFilter implements ArtifactFilter {
    private final Collection<Exclusion> exclusions;

    /**
     * Create a filter.
     * 
     * @param exclusions
     *            A {@link Collection} of {@link Exclusion} objects representing the artifacts to be excluded.
     */
    public ExclusionArtifactFilter(Collection<Exclusion> exclusions) {
        this.exclusions = Collections.unmodifiableCollection(exclusions);
    }

    @Override
    public boolean include(Artifact artifact) {
        for (Exclusion exclusion : exclusions) {
            if (exclusion.matches(artifact)) {
                return false;
            }
        }
        return true;
    }

}
