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
 * Integration tests for {@link AbstractFlumePluginMojo}.
 * 
 * @author Joshua Hyde
 */

public class BuildProjectPluginMojoITest extends AbstractFlumePluginMojoITest {
    /**
     * Test the building of a Flume plugin out of a project.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testBuildProjectPlugin() throws Exception {
        final String projectName = "test-project";
        testProjectPluginAssembly(projectName, projectName);
        verifyPluginInstallation(projectName, projectName, true);
    }

    /**
     * Test the building of a Flume plugin out of a project when a dependency is excluded.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testBuildProjectPluginExclude() throws Exception {
        final String projectName = "test-project-exclude";
        final Collection<String> dependencies = new ArrayList<String>(getExpectedDependencies());
        assertThat(dependencies.remove("slf4j-log4j12-1.6.1.jar")).isTrue();
        testProjectPluginAssembly(projectName, projectName, dependencies);
    }

    /**
     * Test the building of a Flume plugin out of a project.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testBuildProjectDifferentPluginName() throws Exception {
        final String projectName = "test-project-different-plugin-name";
        final String pluginName = "different-plugin";
        testProjectPluginAssembly(projectName, pluginName);
        verifyPluginInstallation(projectName, pluginName, true);
    }

    /**
     * Test the building of a project without attaching the Flume plugin.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testBuildProjectUnattached() throws Exception {
        final String projectName = "test-project-unattached";
        testProjectPluginAssembly(projectName, projectName);
        verifyPluginInstallation(projectName, projectName, false);
    }

    /**
     * Test the assembly of a project into a Flume plugin.
     * 
     * @param projectName
     *            The name of the project to be assembled.
     * @param pluginName
     *            The name of the plugin to be assembled.
     * @throws Exception
     *             If any errors occur during the verification.
     */
    private void testProjectPluginAssembly(String projectName, String pluginName) throws Exception {
        testProjectPluginAssembly(projectName, pluginName, getExpectedDependencies());
    }

    /**
     * Test the assembly of a project into a Flume plugin.
     * 
     * @param projectName
     *            The name of the project to be assembled.
     * @param pluginName
     *            The name of the plugin to be assembled.
     * @param dependencies
     *            A {@link Collection} of filenames expected to be packaged as dependencies of the plugin.
     * @throws Exception
     *             If any errors occur during the verification.
     */
    private void testProjectPluginAssembly(String projectName, String pluginName, Collection<String> dependencies) throws Exception {
        final InvocationResult result = buildProject(projectName, LifecyclePhase.INSTALL);
        assertThat(result.getExitCode()).isZero();

        final File targetDirectory = new File(getTestProjectDirectory(projectName), "target");
        assertThat(targetDirectory).exists();

        final File flumePluginTarGz = new File(targetDirectory, formatPluginFilename(projectName, pluginName, getTestProjectVersion()));
        assertThat(flumePluginTarGz).exists();

        final File gunzipped = new File(getTestDirectory(), FileUtils.removeExtension(flumePluginTarGz.getName()));
        final File untarredDirectory = new File(getTestDirectory(), "untarred");
        final ArchiveUtils archiveUtils = getArchiveUtils();
        archiveUtils.gunzipFile(flumePluginTarGz, gunzipped);
        archiveUtils.untarFile(gunzipped, untarredDirectory);

        final File pluginDirectory = new File(untarredDirectory, pluginName);
        assertThat(pluginDirectory).exists();

        final File libDirectory = new File(pluginDirectory, "lib");
        assertThat(libDirectory).exists();
        assertThat(new File(libDirectory, String.format("%s-%s.jar", projectName, getTestProjectVersion()))).exists();

        final File libExtDirectory = new File(pluginDirectory, "libext");
        assertThat(libExtDirectory).exists();
        for (String flumeDependency : dependencies) {
            assertThat(new File(libExtDirectory, flumeDependency)).exists();
        }

        final List<String> libExtFiles = new ArrayList<String>(FileUtils.getFileNames(libExtDirectory, null, null, false));
        libExtFiles.removeAll(dependencies);
        assertThat(libExtFiles).isEmpty();

        // Although JUnit is listed as a dependency, it should not be included because it's a test dependency
        assertThat(new File(libExtDirectory, "junit-4.11.jar")).doesNotExist();
    }

    /**
     * Get a collection of the expected dependencies packed into the Flume plugin.
     * 
     * @return A {@link Collection} of filenames that are expected to be packaged as dependencies for the Flume plugin.
     */
    private Collection<String> getExpectedDependencies() {
        final List<String> dependencies = new ArrayList<String>(getDependencies("flume-hdfs-sink"));
        dependencies.add("flume-hdfs-sink-1.4.0.jar");
        return dependencies;
    }
}
