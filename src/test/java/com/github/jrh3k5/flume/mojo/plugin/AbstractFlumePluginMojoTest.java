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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import com.github.jrh3k5.flume.mojo.internal.AbstractUnitTest;

/**
 * Unit tests for {@link AbstractFlumePluginMojo}.
 * <p />
 * At this time, there are no unit tests for the {@link AbstractFlumePluginMojo#buildFlumePluginArchive(File, MavenProject)} because the integration tests cover it and the complexity of the method and
 * mocking its objects outweigh the benefit of unit testing it.
 * 
 * @author Joshua Hyde
 */

@RunWith(MockitoJUnitRunner.class)
public class AbstractFlumePluginMojoTest extends AbstractUnitTest {
    private final String classifier = UUID.randomUUID().toString();
    @Mock
    private ArtifactRepository artifactRepository;
    @Mock
    private ArtifactResolver artifactResolver;
    @Mock
    private DependencyGraphBuilder dependencyGraphBuilder;
    @Mock
    private MavenProject project;
    @Mock
    private MavenProjectHelper projectHelper;
    @Mock
    private ArtifactRepository remoteArtifactRepository;

    private ConcreteMojo mojo;
    private File outputDirectory;
    private File pluginsStagingDirectory;

    /**
     * Set up the mojo for each test.
     * 
     * @throws Exception
     *             If any errors occur during the setup.
     */
    @Before
    public void setUp() throws Exception {
        mojo = new ConcreteMojo(getTestName());
        Whitebox.setInternalState(mojo, "artifactRepository", artifactRepository);
        Whitebox.setInternalState(mojo, "artifactResolver", artifactResolver);
        setAttach(mojo, true);
        Whitebox.setInternalState(mojo, "classifier", classifier);
        Whitebox.setInternalState(mojo, "dependencyGraphBuilder", dependencyGraphBuilder);

        outputDirectory = new File(getTestDirectory(), "target");
        FileUtils.forceMkdir(outputDirectory);
        Whitebox.setInternalState(mojo, "outputDirectory", outputDirectory);

        pluginsStagingDirectory = new File(outputDirectory, "flume-plugins");
        FileUtils.forceMkdir(pluginsStagingDirectory);
        Whitebox.setInternalState(mojo, "pluginsStagingDirectory", pluginsStagingDirectory);

        Whitebox.setInternalState(mojo, "project", project);
        Whitebox.setInternalState(mojo, "projectHelper", projectHelper);
        Whitebox.setInternalState(mojo, "remoteArtifactRepositories", Collections.singletonList(remoteArtifactRepository));
    }

    /**
     * Test the formatting of an identifier for an {@link Artifact} object.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testFormatIdentifierArtifact() throws Exception {
        final Artifact artifact = mock(Artifact.class);
        final String groupId = UUID.randomUUID().toString();
        when(artifact.getGroupId()).thenReturn(groupId);
        final String artifactId = UUID.randomUUID().toString();
        when(artifact.getArtifactId()).thenReturn(artifactId);
        final String version = UUID.randomUUID().toString();
        when(artifact.getVersion()).thenReturn(version);
        final String type = UUID.randomUUID().toString();
        when(artifact.getType()).thenReturn(type);
        assertThat(AbstractFlumePluginMojo.formatIdentifier(artifact)).isEqualTo(String.format("%s:%s:%s:%s", groupId, artifactId, version, type));
    }

    /**
     * Test the formatting of an identifier for a {@link MavenProject} object.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testFormatIdentifierMavenProject() throws Exception {
        final MavenProject project = mock(MavenProject.class);
        final String groupId = UUID.randomUUID().toString();
        when(project.getGroupId()).thenReturn(groupId);
        final String artifactId = UUID.randomUUID().toString();
        when(project.getArtifactId()).thenReturn(artifactId);
        final String version = UUID.randomUUID().toString();
        when(project.getVersion()).thenReturn(version);
        final String type = UUID.randomUUID().toString();
        when(project.getPackaging()).thenReturn(type);
        assertThat(AbstractFlumePluginMojo.formatIdentifier(project)).isEqualTo(String.format("%s:%s:%s:%s", groupId, artifactId, version, type));
    }

    /**
     * Test the retrieval of the stored {@link ArtifactRepository}.
     */
    @Test
    public void testGetArtifactRepository() {
        assertThat(mojo.getArtifactRepository()).isEqualTo(artifactRepository);
    }

    /**
     * Test the retrieval of the stored {@link MavenProject}.
     */
    @Test
    public void testGetProject() {
        assertThat(mojo.getProject()).isEqualTo(project);
    }

    /**
     * Test the retrieval of the stored remote repositories.
     */
    @Test
    public void testRemoteArtifactRepositories() {
        assertThat(mojo.getRemoteArtifactRepositories()).isEqualTo(Collections.<ArtifactRepository> singletonList(remoteArtifactRepository));
    }

    /**
     * Set whether or not the created artifact should be attached.
     * 
     * @param mojo
     *            The {@link AbstractFlumePluginMojo} in which the property is to be set.
     * @param attach
     *            {@code true} if the artifact should be attached; {@code false} if not.
     */
    private void setAttach(AbstractFlumePluginMojo mojo, boolean attach) {
        Whitebox.setInternalState(mojo, "attach", attach);
    }

    /**
     * Concrete implementation of {@link AbstractFlumePluginMojo} for testing purposes.
     * 
     * @author Joshua Hyde
     */
    private static class ConcreteMojo extends AbstractFlumePluginMojo {
        private final String pluginName;

        /**
         * Create a mojo.
         * 
         * @param pluginName
         *            The name of the Flume plugin to be created.
         */
        public ConcreteMojo(String pluginName) {
            this.pluginName = pluginName;
        }

        @Override
        public void execute() throws MojoExecutionException, MojoFailureException {
        }

        @Override
        protected String getPluginName() {
            return pluginName;
        }

    }
}
