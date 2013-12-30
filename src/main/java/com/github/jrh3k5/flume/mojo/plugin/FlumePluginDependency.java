package com.github.jrh3k5.flume.mojo.plugin;

/**
 * Definition of the dependency to be resolved and bundled into a Flume plugin.
 * 
 * @author Joshua Hyde
 */

public class FlumePluginDependency {
    private String groupId;
    private String artifactId;

    /**
     * Get the group ID of the dependency to be bundled.
     * 
     * @return The group ID of the dependency to be bundled.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Set the group ID of the dependency to be bundled.
     * 
     * @param groupId
     *            The group ID of the dependency to be bundled.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Get the artifact ID to be resolved into a Flume plugin.
     * 
     * @return The artifact ID to be resolved into a Flume plugin.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Set the artifact ID to be resolved into a Flume plugin.
     * 
     * @param artifactId
     *            The artifact ID to be resolved into a Flume plugin.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Get the formatted identifier of this dependency.
     * 
     * @return The formatted identifier of this dependency.
     */
    public String getFormattedIdentifier() {
        return String.format("%s:%s", getGroupId(), getArtifactId());
    }
}
