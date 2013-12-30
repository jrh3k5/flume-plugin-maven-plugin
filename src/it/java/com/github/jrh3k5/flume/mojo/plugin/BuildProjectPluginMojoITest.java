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

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.shared.invoker.InvocationResult;
import org.junit.Test;

import com.github.jrh3k5.flume.mojo.plugin.io.ArchiveUtils;

/**
 * Integration tests for {@link AbstractFlumePluginMojo}.
 * 
 * @author Joshua Hyde
 */

public class BuildProjectPluginMojoITest extends AbstractFlumePluginMojoITest {
    /**
     * Test the building of a plugin out of a Flume project.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testBuildProjectPlugin() throws Exception {
        final String projectName = "test-project";
        final InvocationResult result = buildProject(projectName, LifecyclePhase.PACKAGE);
        assertThat(result.getExitCode()).isZero();

        final File targetDirectory = new File(getProjectDirectory(projectName), "target");
        assertThat(targetDirectory).exists();

        final File flumePluginTarGz = new File(targetDirectory, "test-project-1.0-SNAPSHOT-flume-plugin.tar.gz");
        assertThat(flumePluginTarGz).exists();

        final File gunzipped = new File(getTestDirectory(), "test-project-1.0-SNAPSHOT-flume-plugin.tar");
        final File untarredDirectory = new File(getTestDirectory(), "untarred");
        final ArchiveUtils archiveUtils = getArchiveUtils();
        archiveUtils.gunzipFile(flumePluginTarGz, gunzipped);
        archiveUtils.untarFile(gunzipped, untarredDirectory);

        final File pluginDirectory = new File(untarredDirectory, projectName);
        assertThat(pluginDirectory).exists();

        final File libDirectory = new File(pluginDirectory, "lib");
        assertThat(libDirectory).exists();
        assertThat(new File(libDirectory, "test-project-1.0-SNAPSHOT.jar")).exists();

        final File libExtDirectory = new File(pluginDirectory, "libext");
        assertThat(libExtDirectory).exists();
        assertThat(new File(libExtDirectory, "flume-hdfs-sink-1.4.0.jar")).exists();
        for (String flumeDependency : getDependencies("flume-hdfs-sink")) {
            assertThat(new File(libExtDirectory, flumeDependency)).exists();
        }

        // Although JUnit is listed as a dependency, it should not be included because it's a test dependency
        assertThat(new File(libExtDirectory, "junit-4.11.jar")).doesNotExist();
    }
}
