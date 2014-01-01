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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.test.plugin.BuildTool;
import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import com.github.jrh3k5.flume.mojo.plugin.io.ArchiveUtils;

/**
 * Test harness for plugin testing.
 * 
 * @author Joshua Hyde
 */

public abstract class AbstractFlumePluginMojoITest {
    private static final Properties ORIGINAL_SYSTEM_PROPERTIES = System.getProperties();
    private static final Map<String, Collection<String>> TRANSITIVE_DEPENDENCIES = new HashMap<String, Collection<String>>();

    static {
        final InputStream hdfsDependenciesStream = AbstractFlumePluginMojo.class.getResourceAsStream("/flume-hdfs-sink.dependencies");
        try {
            TRANSITIVE_DEPENDENCIES.put("flume-hdfs-sink", IOUtils.readLines(hdfsDependenciesStream));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        } finally {
            IOUtils.closeQuietly(hdfsDependenciesStream);
        }
    }

    /**
     * Create a directory.
     * 
     * @param directory
     *            The directory to be created.
     */
    private static void createDirectory(File directory) {
        try {
            FileUtils.forceMkdir(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create directory: " + directory, e);
        }
    }

    /**
     * A {@link Rule} used to obtain the current test name.
     */
    @Rule
    public TestName testName = new TestName();
    private final File localRepository = new File("target/integration-test/repository");
    private BuildTool build;

    /**
     * Set the {@code $maven.home} value in the system properties for the {@link BuildTool} object.
     * 
     * @throws Exception
     *             If any errors occur during the setup.
     */
    @Before
    public void setUpMavenHome() throws Exception {
        final ResourceBundle systemPropsBundle = ResourceBundle.getBundle("system");
        final String mavenHome = systemPropsBundle.getString("maven.home");
        if (mavenHome == null) {
            throw new IllegalStateException("maven.home is null; did the resources not filter correctly?");
        }
        if (!new File(mavenHome).exists()) {
            throw new IllegalStateException(String.format("Configured maven.home '%s' does not exist.", mavenHome));
        }
        System.setProperty("maven.home", mavenHome);
    }

    /**
     * Create and initialize a {@link BuildTool} object to invoke Maven.
     * 
     * @throws Exception
     *             If any errors occur during the setup.
     */
    @Before
    public void setUpBuildTool() throws Exception {
        build = new BuildTool();
        build.initialize();
    }

    /**
     * Clean up resources used by the (possibly) instantiated {@link BuildTool} object.
     * 
     * @throws Exception
     *             If any errors occur during the teardown.
     */
    @After
    public void disposeOfBuildTool() throws Exception {
        if (build != null)
            build.dispose();
    }

    /**
     * Restore the system properties to their original values.
     * 
     * @throws Exception
     *             If any errors occur during the restoration.
     */
    @After
    public void restoreSystemProperties() throws Exception {
        System.setProperties(ORIGINAL_SYSTEM_PROPERTIES);
    }

    /**
     * Build a project.
     * 
     * @param artifactId
     *            The artifact ID of the project to build.
     * @param goal
     *            The goal to invoke.
     * @return An {@link InvocationResult} indicating the result of the build.
     * @throws Exception
     *             If any errors occur during the build.
     */
    protected InvocationResult buildProject(String artifactId, LifecyclePhase goal) throws Exception {
        final Properties buildProps = new Properties();
        buildProps.putAll(getBuildArguments());
        final InvocationRequest request = build.createBasicInvocationRequest(getPom(artifactId), buildProps, Collections.singletonList(goal.id()), getLogFile());
        request.setShowErrors(true);
        return build.executeMaven(request);
    }

    /**
     * Format a plugin filename.
     * 
     * @param pluginName
     *            The name of the plugin.
     * @param version
     *            The verison of the plugin.
     * @return The name of the plugin file.
     */
    protected String formatPluginFilename(String projectName, String pluginName, String version) {
        // The plugin won't generate an artifact with anything other than the normal classifier suffix if the plugin name is the same as the artifact ID
        if (projectName.equals(pluginName)) {
            return String.format("%s-%s-flume-plugin.tar.gz", projectName, version);
        }
        return String.format("%s-%s-%s-flume-plugin.tar.gz", projectName, version, pluginName);
    }

    /**
     * Get archive utilities.
     * 
     * @return Archive utilities.
     */
    protected ArchiveUtils getArchiveUtils() {
        return ArchiveUtils.getInstance(new QuiescentLogger(getTestName()));
    }

    /**
     * Get a list of the arguments to be used at the build.
     * 
     * @return A {@link List} of arguments to be used in the Maven build.
     */
    protected Map<String, String> getBuildArguments() {
        return Collections.singletonMap("maven.repo.local", getLocalRepository().getAbsolutePath());
    }

    /**
     * Get the dependencies for the given artifact.
     * 
     * @param artifactId
     *            The ID of the artifact whose dependencies are to be retrieved.
     * @return A {@link Collection} representing the dependencies for the given artifact.
     * @throws IllegalArgumentException
     *             If no dependencies are found for the given artifact.
     */
    protected Collection<String> getDependencies(String artifactId) {
        final Collection<String> dependencies = TRANSITIVE_DEPENDENCIES.get(artifactId);
        if (dependencies == null) {
            throw new IllegalArgumentException("No dependencies found for artifact ID: " + artifactId);
        }
        return Collections.unmodifiableCollection(dependencies);
    }

    /**
     * Get the location of the local repository used by the build.
     * 
     * @return A {@link File} object representing the location of the local repository used by the build.
     */
    protected File getLocalRepository() {
        return localRepository;
    }

    /**
     * Create a log file to which a Maven invocation can write its output.
     * 
     * @return A {@link File} representing a location to which the Maven invocation can write its output.
     */
    protected File getLogFile() {
        final File logDirectory = new File("target/logs/" + getClass().getSimpleName());
        if (!logDirectory.exists())
            try {
                FileUtils.forceMkdir(logDirectory);
            } catch (final IOException e) {
                throw new RuntimeException("Failed to create log directory.", e);
            }

        return new File(logDirectory, getTestName() + ".log");
    }

    /**
     * Get a project's POM.
     * 
     * @param artifactId
     *            The artifact ID of the project whose POM is to be retrieved.
     * @return A {@link File} reference representing the desired POM file.
     * @throws FileNotFoundException
     *             If the given file cannot be found on the classpath.
     * @throws URISyntaxException
     *             If converting the {@link URL} representing the file on the classpath cannot be converted into a {@link URI}.
     */
    protected File getPom(final String artifactId) throws FileNotFoundException, URISyntaxException {
        final URL resourceUrl = getClass().getResource("/flume-plugin-maven-plugin-test-projects/" + getClass().getSimpleName() + "/" + artifactId + "/pom.xml");
        if (resourceUrl == null)
            throw new FileNotFoundException("Project POM not found: " + artifactId);
        return FileUtils.toFile(resourceUrl);
    }

    /**
     * Get a directory isolated to the current test that can be used by the current test.
     * 
     * @return A {@link File} representing a directory to contain files for the current test.
     */
    protected File getTestDirectory() {
        final File classDirectory = new File("target", getClass().getSimpleName());
        createDirectory(classDirectory);

        final File testDirectory = new File(classDirectory, getTestName());
        createDirectory(testDirectory);

        return testDirectory;
    }

    /**
     * Get the name of the current test.
     * 
     * @return The name of the test.
     */
    protected String getTestName() {
        return testName.getMethodName();
    }

    /**
     * Get a project's project directory.
     * 
     * @param projectName
     *            The name of the project for which the project is to be retrieved.
     * @return A {@link File} representing the given project's project directory.
     * @throws FileNotFoundException
     *             If the given project's project directory cannot be found.
     */
    protected File getTestProjectDirectory(final String projectName) throws FileNotFoundException {
        final URL projectUrl = getClass().getResource("/flume-plugin-maven-plugin-test-projects/" + getClass().getSimpleName() + "/" + projectName);
        if (projectUrl == null) {
            throw new FileNotFoundException("Project not found: " + projectName);
        }
        return FileUtils.toFile(projectUrl);
    }

    /**
     * Get the group ID of the test projects.
     * 
     * @return The group ID of the test projects.
     */
    protected String getTestProjectGroupId() {
        return "com.github.jrh3k5";
    }

    /**
     * Get the version of the test project.
     * 
     * @return The version of the test project.
     */
    protected String getTestProjectVersion() {
        return "1.0-SNAPSHOT";
    }

    /**
     * Verify that the plugin was installed as expected.
     * 
     * @param projectName
     *            The name of the project that was installed.
     * @param pluginName
     *            The name of the plugin as expected.
     * @param expectInstalled
     *            {@code true} if the plugin should be expected to be installed; {@code false} if not.
     */
    protected void verifyPluginInstallation(String projectName, String pluginName, boolean expectInstalled) {
        final String groupIdPath = getTestProjectGroupId().replace('.', File.separatorChar);
        final File groupIdDir = new File(getLocalRepository(), groupIdPath);
        assertThat(groupIdDir).exists().isDirectory();
        final File artifactDir = new File(groupIdDir, projectName);
        assertThat(artifactDir).exists().isDirectory();
        final File artifactVersionDir = new File(artifactDir, getTestProjectVersion());
        assertThat(artifactVersionDir).exists().isDirectory();
        final File pluginFile = new File(artifactVersionDir, formatPluginFilename(projectName, pluginName, getTestProjectVersion()));
        if (expectInstalled) {
            assertThat(pluginFile).exists();
        } else {
            assertThat(pluginFile).doesNotExist();
        }
    }

    /**
     * A quiescent logger.
     * 
     * @author Joshua Hyde
     */
    private static class QuiescentLogger extends AbstractLogger {
        /**
         * Create a logger.
         * 
         * @param name
         *            The name of the logger.
         */
        public QuiescentLogger(String name) {
            super(Logger.LEVEL_ERROR, name);
        }

        public void debug(String message, Throwable throwable) {
        }

        public void info(String message, Throwable throwable) {
        }

        public void warn(String message, Throwable throwable) {
        }

        public void error(String message, Throwable throwable) {
        }

        public void fatalError(String message, Throwable throwable) {
        }

        public Logger getChildLogger(String name) {
            return new QuiescentLogger(name);
        }
    }
}
