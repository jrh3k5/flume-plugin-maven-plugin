package com.github.jrh3k5.flume.mojo.plugin;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;

/**
 * A representation of a dependency to exclude from assembly.
 * 
 * @author Joshua Hyde
 */

public class Exclusion {
    private String groupId;
    private String artifactId;
    private String classifier;

    /**
     * Get the artifact ID of the artifact to be excluded.
     * 
     * @return The artifact ID of the artifact to be excluded.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Get the classifier of the artifact to be excluded.
     * 
     * @return {@code null} if no classifier has been set; otherwise, the classifier of the artifact to be excluded.
     * @since 1.1
     */
    public String getClassifier() {
        return classifier;
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
     * Set the optional classifier by which to match an artifact.
     * 
     * @param classifier
     *            The classifier of an artifact to be excluded.
     * @since 1.1
     */
    public void setClassifier(String classifier) {
        this.classifier = classifier;
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
        return safeCompare(getGroupId(), artifact.getGroupId()) && safeCompare(getArtifactId(), artifact.getArtifactId()) && safeCompare(getClassifier(), artifact.getClassifier());
    }

    /**
     * Safely compare two strings in a {@code null}-tolerant fashion.
     * 
     * @param first
     *            The first value to be compared.
     * @param second
     *            The second value to be compared.
     * @return {@code true} if they are both {@code null} or they are both equal {@link String} objects; {@code false} if not.
     * @since 1.1
     */
    private boolean safeCompare(String first, String second) {
        if (StringUtils.isEmpty(first)) {
            return StringUtils.isEmpty(second);
        }

        return first.equals(second);
    }
}
