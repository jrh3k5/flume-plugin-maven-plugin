/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jrh3k5.flume.mojo.plugin;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import com.github.jrh3k5.flume.mojo.plugin.artifact.FlumePluginDependencyArtifactFilter;

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
     * The name of the plugin to be created. If not specified, this will inherit the name of the artifact specified as the dependency.
     */
    @Parameter
    private String pluginName;

    /**
     * A {@link MavenProjectBuilder} used to resolve an {@link Artifact} into a {@link MavenProject} for dependency resolution.
     */
    @Component(hint = "default")
    private MavenProjectBuilder projectBuilder;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // Find the plugin in the project dependencies
        final List<DependencyNode> projectChildren = resolveDependencies(getProject(), new FlumePluginDependencyArtifactFilter(dependency));
        if (projectChildren.isEmpty()) {
            throw new MojoFailureException(String.format("No dependency found matching %s in dependency list.", dependency.getFormattedIdentifier()));
        } else if (projectChildren.size() > 1) {
            throw new MojoFailureException(String.format("More than one dependency matching %s found in project dependencies: %s", dependency.getFormattedIdentifier(), projectChildren));
        }

        // Resolve the dependencies of the project we've located
        final Artifact projectChildArtifact = projectChildren.get(0).getArtifact();
        final File projectChildFile = getArtifactRepository().find(projectChildArtifact).getFile();

        try {
            buildFlumePluginArchive(projectChildFile, projectBuilder.buildFromRepository(projectChildArtifact, getRemoteArtifactRepositories(), getArtifactRepository()));
        } catch (ProjectBuildingException e) {
            throw new MojoExecutionException(String.format("Failed to resolve project for artifact %s", formatIdentifier(projectChildArtifact)), e);
        }
    }

    @Override
    protected String getPluginName() {
        return pluginName == null ? dependency.getArtifactId() : pluginName;
    }
}
