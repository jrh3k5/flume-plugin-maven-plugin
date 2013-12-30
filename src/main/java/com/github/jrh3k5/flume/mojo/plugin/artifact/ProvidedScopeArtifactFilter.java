package com.github.jrh3k5.flume.mojo.plugin.artifact;

import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

public class ProvidedScopeArtifactFilter implements ArtifactFilter {
    private static final Set<String> ACCEPTED_SCOPES;
    private static final ProvidedScopeArtifactFilter INSTANCE = new ProvidedScopeArtifactFilter();

    public static ProvidedScopeArtifactFilter getInstance() {
        return INSTANCE;
    }

    static {
        ACCEPTED_SCOPES = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        ACCEPTED_SCOPES.add("COMPILE");
        ACCEPTED_SCOPES.add("RUNTIME");
        ACCEPTED_SCOPES.add("PROVIDED");
    }

    private ProvidedScopeArtifactFilter() {

    }

    @Override
    public boolean include(Artifact artifact) {
        return ACCEPTED_SCOPES.contains(artifact.getScope());
    }

}
