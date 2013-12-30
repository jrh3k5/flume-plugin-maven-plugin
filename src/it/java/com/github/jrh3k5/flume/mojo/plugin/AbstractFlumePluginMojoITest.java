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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
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
    private static final Properties EMPTY_PROPERTIES = new Properties();
    private static final Map<String, Collection<String>> TRANSITIVE_DEPENDENCIES = new HashMap<String, Collection<String>>();

    static {
        TRANSITIVE_DEPENDENCIES.put("flume-hdfs-sink", Arrays.asList("commons-io-2.1.jar", "commons-lang-2.5.jar", "flume-ng-configuration-1.4.0.jar", "flume-ng-core-1.4.0.jar",
                "flume-ng-sdk-1.4.0.jar", "guava-10.0.1.jar", "slf4j-api-1.6.1.jar"));
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
        System.setProperty("maven.home", systemPropsBundle.getString("maven.home"));
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
        final InvocationRequest request = build.createBasicInvocationRequest(getPom(artifactId), EMPTY_PROPERTIES, Collections.singletonList(goal.id()), getLogFile());
        request.setShowErrors(true);
        return build.executeMaven(request);
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
     * Get a project's project directory.
     * 
     * @param projectName
     *            The name of the project for which the project is to be retrieved.
     * @return A {@link File} representing the given project's project directory.
     * @throws FileNotFoundException
     *             If the given project's project directory cannot be found.
     */
    protected File getProjectDirectory(final String projectName) throws FileNotFoundException {
        final URL projectUrl = getClass().getResource("/flume-plugin-maven-plugin-test-projects/" + getClass().getSimpleName() + "/" + projectName);
        if (projectUrl == null) {
            throw new FileNotFoundException("Project not found: " + projectName);
        }
        return FileUtils.toFile(projectUrl);
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
