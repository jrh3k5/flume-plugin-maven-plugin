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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.shared.invoker.InvocationResult;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

import com.github.jrh3k5.flume.mojo.plugin.io.ArchiveUtils;

/**
 * Integration tests for {@link BuildDependencyPluginMojo}.
 * 
 * @author Joshua Hyde
 */

public class BuildDependencyPluginMojoITest extends AbstractFlumePluginMojoITest {
    /**
     * Test build a dependency into a Flume plugin.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testBuildDependencyFlumePlugin() throws Exception {
        final String projectName = "flume-hdfs-sink-test-project";
        final String pluginName = "different-name";
        testBuildDependencyFlumePlugin(projectName, pluginName);
        verifyPluginInstallation(projectName, pluginName, true);
    }

    /**
     * Test building a dependency into a Flume plugin with no name specified. It should inherit the name of the specified dependency.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testBuildDependencyFlumePluginExcludes() throws Exception {
        final String projectName = "flume-hdfs-sink-test-project-exclude";
        final String pluginName = "flume-hdfs-sink";
        final Collection<String> dependencies = new ArrayList<String>(getExpectedDependencies());
        assertThat(dependencies.remove("slf4j-log4j12-1.6.1.jar")).isTrue();
        testBuildDependencyFlumePlugin(projectName, pluginName, dependencies);
    }

    /**
     * Test building a dependency into a Flume plugin with no name specified. It should inherit the name of the specified dependency.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testBuildDependencyFlumePluginNoName() throws Exception {
        final String projectName = "flume-hdfs-sink-test-project-no-plugin-name";
        final String pluginName = "flume-hdfs-sink";
        testBuildDependencyFlumePlugin(projectName, pluginName);
        verifyPluginInstallation(projectName, pluginName, true);
    }

    /**
     * If the plugin isn't configured to attach the artifact, it shouldn't be attached.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testBuildDependencyFlumePluginUnattached() throws Exception {
        final String projectName = "flume-hdfs-sink-test-project-unattached";
        final String pluginName = "flume-hdfs-sink";
        testBuildDependencyFlumePlugin(projectName, pluginName);
        verifyPluginInstallation(projectName, pluginName, false);
    }

    /**
     * Test building a dependency into a Flume plugin.
     * 
     * @param projectName
     *            The name of the project to be built.
     * @param pluginName
     *            The name of the plugin that is expected to be constructed.
     * @throws Exception
     *             If any errors occur during the test run.
     */
    private void testBuildDependencyFlumePlugin(String projectName, String pluginName) throws Exception {
        testBuildDependencyFlumePlugin(projectName, pluginName, getExpectedDependencies());
    }

    /**
     * Test building a dependency into a Flume plugin.
     * 
     * @param projectName
     *            The name of the project to be built.
     * @param pluginName
     *            The name of the plugin that is expected to be constructed.
     * @param expectedDependencies
     *            A {@link Collection} of filenames expected to be packaged as dependencies of the plugin.
     * @throws Exception
     *             If any errors occur during the test run.
     */
    private void testBuildDependencyFlumePlugin(String projectName, String pluginName, Collection<String> expectedDependencies) throws Exception {
        final InvocationResult result = buildProject(projectName, LifecyclePhase.INSTALL);
        assertThat(result.getExitCode()).isZero();

        final File projectTarget = new File(getTestProjectDirectory(projectName), "target");
        final File pluginFile = new File(projectTarget, formatPluginFilename(projectName, pluginName, getTestProjectVersion()));
        assertThat(pluginFile).exists();

        final File testDirectory = getTestDirectory();
        final File tarFile = new File(testDirectory, String.format("%s-1.0-SNAPSHOT-flume-plugin.tar", pluginName));
        final File untarredDirectory = new File(testDirectory, "untarred");

        final ArchiveUtils archiveUtils = getArchiveUtils();
        archiveUtils.gunzipFile(pluginFile, tarFile);
        archiveUtils.untarFile(tarFile, untarredDirectory);

        final File pluginDirectory = new File(untarredDirectory, pluginName);
        assertThat(pluginDirectory).exists();

        final File libDirectory = new File(pluginDirectory, "lib");
        final String libFilename = "flume-hdfs-sink-1.4.0.jar";
        assertThat(libDirectory).exists();
        assertThat(new File(libDirectory, libFilename)).exists();

        final File libExtDirectory = new File(pluginDirectory, "libext");
        assertThat(libExtDirectory).exists();
        for (String jarFile : expectedDependencies) {
            assertThat(new File(libExtDirectory, jarFile)).exists();
        }

        final List<String> libExtFiles = new ArrayList<String>(FileUtils.getFileNames(libExtDirectory, null, null, false));
        libExtFiles.removeAll(expectedDependencies);
        assertThat(libExtFiles).isEmpty();

        // Verify that the sink JAR, itself, was not copied into the libext directory
        assertThat(new File(libExtDirectory, libFilename)).doesNotExist();
    }

    /**
     * Get the expected dependencies of the test project.
     * 
     * @return A {@link Collection} containing the filenames of the expected dependency JARs.
     */
    private Collection<String> getExpectedDependencies() {
        return getDependencies("flume-hdfs-sink");
    }
}
