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
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import com.github.jrh3k5.flume.mojo.plugin.io.ArchiveUtils;
import com.github.jrh3k5.flume.mojo.plugin.plexus.MojoLogger;

/**
 * Skeleton definition of a plugin that assembles a Flume plugin archive.
 * 
 * @author Joshua Hyde
 */
public abstract class AbstractFlumePluginMojo extends AbstractMojo {
    private final ArtifactFilter providedArtifactFilter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);

    /**
     * An {@link ArtifactRepository} used to resolve an artifact into an actual file.
     */
    @Parameter(required = true, readonly = true, defaultValue = "${localRepository}")
    private ArtifactRepository artifactRepository;

    /**
     * An {@link ArtifactResolver} used to copy dependencies.
     */
    @Component
    private ArtifactResolver artifactResolver;

    /**
     * Indicate whether or not the assembly should be attached to the project.
     */
    @Parameter(required = true, defaultValue = "true")
    private boolean attach;

    /**
     * The suffix to be appended to the Flume plugin file.
     */
    @Parameter(required = true, defaultValue = "flume-plugin")
    private String classifierSuffix;

    /**
     * A {@link DependencyGraphBuilder} used to assemble the dependency graph of the project consuming this plugin.
     */
    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;

    /**
     * The directory to which the final artifact should be written to.
     */
    @Parameter(required = true, defaultValue = "${project.build.directory}")
    private File outputDirectory;

    /**
     * The location where the plugin assembly will be staged prior to completion.
     */
    @Parameter(required = true, defaultValue = "${project.build.directory}/flume-plugins")
    private File pluginsStagingDirectory;

    /**
     * A representation of the project executing this plugin.
     */
    @Parameter(required = true, readonly = true, defaultValue = "${project}")
    private MavenProject project;

    /**
     * A {@link MavenProjectHelper} used to attach the created assembly to the deployment.
     */
    @Component(hint = "default")
    private MavenProjectHelper projectHelper;

    /**
     * A {@link List} of {@link ArtifactRepository} objects representing the remote repositories for artifacts.
     */
    @Parameter(required = true, readonly = true, defaultValue = "${project.remoteArtifactRepositories}")
    private List<ArtifactRepository> remoteArtifactRepositories;

    /**
     * A {@link List} of {@link Exclusion} objects representing the artifacts to be excluded from assembly.
     */
    @Parameter
    private List<Exclusion> exclusions = Collections.emptyList();

    /**
     * Format the name of an artifact.
     * 
     * @param artifact
     *            The {@link Artifact} whose name is to be formatted.
     * @return A formatted identifier of the given {@link Artifact}.
     */
    protected static String formatIdentifier(Artifact artifact) {
        return String.format("%s:%s:%s:%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact.getType());
    }

    /**
     * Format the name of a Maven project.
     * 
     * @param mavenProject
     *            The {@link MavenProject} whose name is to be formatted.
     * @return A formatted identifier of the given {@link MavenProject}.
     */
    protected static String formatIdentifier(MavenProject mavenProject) {
        return String.format("%s:%s:%s:%s", mavenProject.getGroupId(), mavenProject.getArtifactId(), mavenProject.getVersion(), mavenProject.getPackaging());
    }

    /**
     * Build a Flume plugin.
     * 
     * @param pluginLibrary
     *            A {@link File} representing the library that is to copied into the {@code lib/} directory of the plugin.
     * @param mavenProject
     *            A {@link MavenProject} representing the project from which dependency information should be read.
     * @throws MojoExecutionException
     *             If any errors occur during the bundling of the plugin archive.
     */
    protected void buildFlumePluginArchive(File pluginLibrary, MavenProject mavenProject) throws MojoExecutionException {
        final String pluginName = getPluginName();
        // Create the directory into which the libraries will be copied
        final File pluginStagingDirectory = new File(pluginsStagingDirectory, String.format("%s-staging", pluginName));
        final File stagingDirectory = new File(pluginStagingDirectory, pluginName);
        try {
            FileUtils.forceMkdir(stagingDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to create directory: " + stagingDirectory.getAbsolutePath(), e);
        }

        final File libDirectory = new File(stagingDirectory, "lib");
        // Copy the primary library
        try {
            FileUtils.copyFile(pluginLibrary, new File(libDirectory, pluginLibrary.getName()));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to copy primary artifact to staging lib directory: " + libDirectory.getAbsolutePath(), e);
        }

        // Copy the dependencies of the plugin into the libext directory
        final File libExtDirectory = new File(stagingDirectory, "libext");
        final AndArtifactFilter joinFilter = new AndArtifactFilter();
        joinFilter.add(providedArtifactFilter);
        joinFilter.add(new ExclusionArtifactFilter(exclusions));
        for (DependencyNode resolvedDependency : resolveDependencies(mavenProject, joinFilter)) {
            copyPluginDependency(resolvedDependency, libExtDirectory);
        }

        // Because of the way that Maven represents dependency trees, the above logic may copy the given plugin library into libext - remove it if so
        final File pluginLibraryLibExt = new File(libExtDirectory, pluginLibrary.getName());
        if (pluginLibraryLibExt.exists()) {
            try {
                FileUtils.forceDelete(pluginLibraryLibExt);
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to delete file: " + pluginLibraryLibExt.getAbsolutePath(), e);
            }
        }

        String classifier = null;
        // If the plugin name is the same as the artifact, then don't bother over-complicating the classifier
        if (project.getArtifactId().equals(pluginName)) {
            classifier = classifierSuffix;
        } else {
            classifier = String.format("%s-%s", pluginName, classifierSuffix);
        }
        final ArchiveUtils archiveUtils = ArchiveUtils.getInstance(new MojoLogger(getLog(), getClass()));
        // Create the TAR
        final File tarFile = new File(pluginStagingDirectory, String.format("%s-%s-%s.tar", project.getArtifactId(), project.getVersion(), classifier));
        try {
            archiveUtils.tarDirectory(pluginStagingDirectory, tarFile);
        } catch (IOException e) {
            throw new MojoExecutionException(String.format("Failed to TAR directory %s to file %s", stagingDirectory.getAbsolutePath(), tarFile.getAbsolutePath()), e);
        }

        // GZIP the TAR file
        final File gzipFile = new File(outputDirectory, String.format("%s.gz", tarFile.getName()));
        try {
            archiveUtils.gzipFile(tarFile, gzipFile);
        } catch (IOException e) {
            throw new MojoExecutionException(String.format("Failed to gzip TAR file %s to %s", tarFile.getAbsolutePath(), gzipFile.getAbsolutePath()), e);
        }

        // Attach the artifact, if configured to do so
        if (attach) {
            projectHelper.attachArtifact(project, "tar.gz", classifier, gzipFile);
        }
    }

    /**
     * Get the artifact repository.
     * 
     * @return An {@link ArtifactRepository}.
     */
    protected ArtifactRepository getArtifactRepository() {
        return artifactRepository;
    }

    /**
     * Get the Maven project.
     * 
     * @return A {@link MavenProject} object representing the current project.
     */
    protected MavenProject getProject() {
        return project;
    }

    /**
     * Get a list of remote repositories that can contain dependencies.
     * 
     * @return A {@link List} of {@link ArtifactRepository} objects representing the remote artifact repositories.
     */
    protected List<ArtifactRepository> getRemoteArtifactRepositories() {
        return remoteArtifactRepositories;
    }

    /**
     * Get the name of the plugin to be assembled.
     * 
     * @return The name of the plugin to be assembled.
     */
    protected abstract String getPluginName();

    /**
     * Resolve dependencies of a project matching the given filter.
     * 
     * @param mavenProject
     *            The {@link MavenProject} whose dependency tree is to be read.
     * @param artifactFilter
     *            An {@link ArtifactFilter} that will determine what artifacts are to qualify.
     * @return A {@link List} of {@link DependencyNode} objects representing the matching dependencies.
     * @throws MojoExecutionException
     *             If any errors occur while trying to resolve the dependencies.
     */
    protected List<DependencyNode> resolveDependencies(MavenProject mavenProject, ArtifactFilter artifactFilter) throws MojoExecutionException {
        try {
            return dependencyGraphBuilder.buildDependencyGraph(project, artifactFilter).getChildren();
        } catch (DependencyGraphBuilderException e) {
            throw new MojoExecutionException(String.format("Failed to build dependency graph for project %s", formatIdentifier(project)), e);
        }
    }

    /**
     * Copy the given plugin dependency - and all of its children - to the {@code libext/} directory.
     * 
     * @param dependencyNode
     *            The {@link DependencyNode} whose artifact and children are to be copied.
     * @param libExtDirectory
     *            A {@link File} representing the directory to which the libraries are to be copied.
     * @throws MojoExecutionException
     *             If any errors occur during the copying.
     */
    private void copyPluginDependency(DependencyNode dependencyNode, File libExtDirectory) throws MojoExecutionException {
        final Artifact resolvedArtifact = artifactRepository.find(dependencyNode.getArtifact());
        try {
            FileUtils.copyFile(resolvedArtifact.getFile(), new File(libExtDirectory, resolvedArtifact.getFile().getName()));
        } catch (IOException e) {
            throw new MojoExecutionException(String.format("Failed to copy artifact %s to %s.", formatIdentifier(resolvedArtifact), libExtDirectory.getAbsolutePath()), e);
        }
        // Recursively copy all other libraries
        for (DependencyNode child : dependencyNode.getChildren()) {
            copyPluginDependency(child, libExtDirectory);
        }
    }
}
