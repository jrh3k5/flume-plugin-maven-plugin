package com.github.jrh3k5.flume.mojo.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * A mojo to build a dependency into a Flume plugin.
 * 
 * @author Joshua Hyde
 */

@Mojo(name = "build-dependency-plugin", defaultPhase = LifecyclePhase.PACKAGE)
public class BuildDependencyPluginMojo extends AbstractFlumePluginMojo {
    /**
     * Defines the dependency that's to be resolved and bundled as a Flume plugin. An example configuration would be:
     * 
     * <pre>
     *  &lt;dependency&gt;
     *    &lt;groupId&gt;org.apache.flume.flume-ng-sink&lt;/groupId&gt;
     *    &lt;artifactId&gt;flume-hdfs-sink&lt;/artifactId&gt;
     *  &lt;/dependency&gt;
     * </pre>
     * 
     * This will attempt to look up a dependency in your project by the given information, resolve its transitive dependencies, and assemble them into a {@code .tar.gz}.
     */
    @Parameter(required = true)
    private FlumePluginDependency dependency;

    /**
     * The name of the plugin to be created.
     */
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
