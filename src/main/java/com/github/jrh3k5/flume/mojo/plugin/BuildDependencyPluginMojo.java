package com.github.jrh3k5.flume.mojo.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "build-dependency-plugin", defaultPhase = LifecyclePhase.PACKAGE)
public class BuildDependencyPluginMojo extends AbstractFlumePluginMojo {
    // TODO: make this look up by groupId and artifactId and don't require the version
    @Parameter(required = true)
    private FlumePluginDependency dependency;

    // TODO: test this
    @Parameter
    private String pluginName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        buildFlumePluginArchive(dependency);
    }

    @Override
    protected String getPluginName() {
        return pluginName == null ? dependency.getArtifactId() : pluginName;
    }

}
