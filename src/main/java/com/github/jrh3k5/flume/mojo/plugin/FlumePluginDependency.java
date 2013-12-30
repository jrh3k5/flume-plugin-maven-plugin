package com.github.jrh3k5.flume.mojo.plugin;

import org.apache.maven.project.MavenProject;

public class FlumePluginDependency {
    private String groupId;
    private String artifactId;
    private String version;

    public FlumePluginDependency() {

    }

    public FlumePluginDependency(MavenProject project) {
        setGroupId(project.getGroupId());
        setArtifactId(getArtifactId());
        setVersion(getVersion());
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFormattedIdentifier() {
        return String.format("%s:%s:%s", getGroupId(), getArtifactId(), getVersion());
    }
}
