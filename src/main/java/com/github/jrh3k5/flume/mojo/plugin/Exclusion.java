package com.github.jrh3k5.flume.mojo.plugin;

import org.apache.maven.artifact.Artifact;

/**
 * A representation of a dependency to exclude from assembly.
 * 
 * @author Joshua Hyde
 */

public class Exclusion {
    private String groupId;
    private String artifactId;

    /**
     * Get the artifact ID of the artifact to be excluded.
     * 
     * @return The artifact ID of the artifact to be excluded.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Get the group ID of the artifact to be excluded.
     * 
     * @return The group ID of the artifact to be excluded.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Set the artifact ID of the artifact to be excluded.
     * 
     * @param artifactId
     *            The artifact ID of the artifact to be excluded.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Set the group ID of the artifact to be excluded.
     * 
     * @param groupId
     *            The group ID of the artifact to be excluded.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Determine whether or not this exclusions matches the given artifact.
     * 
     * @param artifact
     *            The {@link Artifact} against which a comparison is to be made.
     * @returns {@code true} if the given artifact matches this exclusion; {@code false} if not.
     */
    public boolean matches(Artifact artifact) {
        return getGroupId().equals(artifact.getGroupId()) && getArtifactId().equals(artifact.getArtifactId());
    }
}
