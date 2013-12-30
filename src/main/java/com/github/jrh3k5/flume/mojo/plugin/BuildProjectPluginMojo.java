package com.github.jrh3k5.flume.mojo.plugin;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * A mojo that is used to build the current project into a Flume plugin.
 * 
 * @author Joshua Hyde
 */

@Mojo(name = "build-project-plugin", defaultPhase = LifecyclePhase.PACKAGE)
public class BuildProjectPluginMojo extends AbstractFlumePluginMojo {
    /**
     * The name of the plugin.
     */
    @Parameter(required = true, defaultValue = "${project.artifactId}")
    private String pluginName;

    /**
     * The location of where the final artifact that is to be treated as the "primary" library (e.g., the JAR produced by a project) of the plugin.
     */
    @Parameter(required = true, defaultValue = "${project.build.directory}/${project.build.finalName}.${project.packaging}")
    private File pluginLibrary;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        buildFlumePluginArchive(pluginLibrary, getProject());
    }

    @Override
    protected String getPluginName() {
        return pluginName;
    }
}
