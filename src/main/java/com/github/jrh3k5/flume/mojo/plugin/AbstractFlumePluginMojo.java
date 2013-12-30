package com.github.jrh3k5.flume.mojo.plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import com.github.jrh3k5.flume.mojo.plugin.artifact.FlumePluginDependencyArtifactFilter;
import com.github.jrh3k5.flume.mojo.plugin.io.ArchiveUtils;
import com.github.jrh3k5.flume.mojo.plugin.plexus.MojoLogger;

public abstract class AbstractFlumePluginMojo extends AbstractMojo {
    private final ArtifactFilter providedArtifactFilter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);

    /**
     * An {@link ArtifactResolver} used to copy dependencies.
     */
    @Component
    private ArtifactResolver artifactResolver;

    /**
     * A {@link DependencyGraphBuilder} used to assemble the dependency graph of the project consuming this plugin.
     */
    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;

    @Component(hint = "default")
    private MavenProjectBuilder projectBuilder;

    @Parameter(required = true, readonly = true, defaultValue = "${localRepository}")
    private ArtifactRepository artifactRepository;

    @Parameter(required = true, readonly = true, defaultValue = "${project.remoteArtifactRepositories}")
    private List<ArtifactRepository> remoteArtifactRepositories;

    /**
     * A representation of the project executing this plugin.
     */
    @Parameter(required = true, readonly = true, defaultValue = "${project}")
    private MavenProject project;

    /**
     * The location where the plugin assembly will be staged prior to completion.
     */
    @Parameter(required = true, defaultValue = "${project.build.directory}/flume-plugins")
    private File pluginsStagingDirectory;

    @Parameter(required = true, defaultValue = "${project.build.directory}")
    private File outputDirectory;

    // TODO: attach the artifact
    /**
     * Indicate whether or not the assembly should be attached to the project.
     */
    @Parameter(required = true, defaultValue = "true")
    private boolean attach;

    /**
     * A {@link MavenProjectHelper} used to attach the created assembly to the deployment.
     */
    @Component(hint = "default")
    private MavenProjectHelper projectHelper;

    @Parameter(required = true, defaultValue = "flume-plugin")
    private String classifier;

    protected void buildFlumePluginArchive(FlumePluginDependency dependency) throws MojoExecutionException, MojoFailureException {
        // Find the plugin in the project dependencies
        final List<DependencyNode> projectChildren = resolveDependencies(project, new FlumePluginDependencyArtifactFilter(dependency));
        if (projectChildren.isEmpty()) {
            throw new MojoFailureException(String.format("No dependency found matching %s in dependency list.", dependency.getFormattedIdentifier()));
        } else if (projectChildren.size() > 1) {
            throw new MojoFailureException(String.format("More than one dependency matching %s found in project dependencies: %s", dependency.getFormattedIdentifier(), projectChildren));
        }

        final String pluginName = getPluginName();
        // Create the directory into which the libraries will be copied
        final File pluginStagingDirectory = new File(pluginsStagingDirectory, String.format("%s-staging", pluginName));
        final File stagingDirectory = new File(pluginStagingDirectory, pluginName);
        try {
            FileUtils.forceMkdir(stagingDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to create directory: " + stagingDirectory.getAbsolutePath(), e);
        }

        // Resolve the dependencies of the project we've located
        final Artifact projectChildArtifact = projectChildren.get(0).getArtifact();

        // Copy the primary library
        final File libDirectory = new File(stagingDirectory, "lib");
        final File projectChildFile = artifactRepository.find(projectChildArtifact).getFile();
        if (projectChildFile == null) {
            throw new MojoExecutionException(String.format("No artifact file found for %s", formatIdentifier(projectChildArtifact)));
        }
        try {
            FileUtils.copyFile(projectChildFile, new File(libDirectory, projectChildFile.getName()));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to copy primary artifact to staging lib directory: " + libDirectory.getAbsolutePath(), e);
        }

        MavenProject flumeDependencyProject;
        try {
            flumeDependencyProject = projectBuilder.buildFromRepository(projectChildArtifact, remoteArtifactRepositories, artifactRepository);
        } catch (ProjectBuildingException e) {
            throw new MojoExecutionException(String.format("Failed to build project for %s", formatIdentifier(projectChildArtifact)), e);
        }

        final File libExtDirectory = new File(stagingDirectory, "libext");
        for (DependencyNode resolvedDependency : resolveDependencies(flumeDependencyProject, providedArtifactFilter).get(0).getChildren()) {
            final Artifact resolvedArtifact = artifactRepository.find(resolvedDependency.getArtifact());
            try {
                FileUtils.copyFile(resolvedArtifact.getFile(), new File(libExtDirectory, resolvedArtifact.getFile().getName()));
            } catch (IOException e) {
                throw new MojoExecutionException(String.format("Failed to copy artifact %s to %s.", formatIdentifier(resolvedArtifact), libExtDirectory.getAbsolutePath()), e);
            }
        }

        final ArchiveUtils archiveUtils = ArchiveUtils.getInstance(new MojoLogger(getLog(), getClass()));
        final File tarFile = new File(pluginStagingDirectory, String.format("%s-%s-%s.tar", pluginName, project.getVersion(), classifier));
        try {
            archiveUtils.tarDirectory(pluginStagingDirectory, tarFile);
        } catch (IOException e) {
            throw new MojoExecutionException(String.format("Failed to TAR directory %s to file %s", stagingDirectory.getAbsolutePath(), tarFile.getAbsolutePath()), e);
        }

        final File gzipFile = new File(outputDirectory, String.format("%s.gz", tarFile.getName()));
        try {
            archiveUtils.gzipFile(tarFile, gzipFile);
        } catch (IOException e) {
            throw new MojoExecutionException(String.format("Failed to gzip TAR file %s to %s", tarFile.getAbsolutePath(), gzipFile.getAbsolutePath()), e);
        }

        if (attach) {
            projectHelper.attachArtifact(project, "tar.gz", classifier, gzipFile);
        }
    }

    protected abstract String getPluginName();

    private List<DependencyNode> resolveDependencies(MavenProject mavenProject, ArtifactFilter artifactFilter) throws MojoExecutionException {
        try {
            return dependencyGraphBuilder.buildDependencyGraph(project, artifactFilter).getChildren();
        } catch (DependencyGraphBuilderException e) {
            throw new MojoExecutionException(String.format("Failed to build dependency graph for project %s", formatIdentifier(project)), e);
        }
    }

    private static String formatIdentifier(Artifact artifact) {
        return String.format("%s:%s:%s:%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact.getType());
    }

    private static String formatIdentifier(MavenProject mavenProject) {
        return String.format("%s:%s:%s:%s", mavenProject.getGroupId(), mavenProject.getArtifactId(), mavenProject.getVersion(), mavenProject.getPackaging());
    }
}
