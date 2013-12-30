package com.github.jrh3k5.flume.mojo.plugin;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.shared.invoker.InvocationResult;
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
        testBuildDependencyFlumePlugin("flume-hdfs-sink-test-project", "different-name");
    }

    /**
     * Test building a dependency into a Flume plugin with no name specified. It should inherit the name of the specified dependency.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testBuildDependencyFlumePluginNoName() throws Exception {
        testBuildDependencyFlumePlugin("flume-hdfs-sink-test-project-no-plugin-name", "flume-hdfs-sink");
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
        final InvocationResult result = buildProject(projectName, LifecyclePhase.PACKAGE);
        assertThat(result.getExitCode()).isZero();

        final File projectTarget = new File(getProjectDirectory(projectName), "target");
        final File pluginFile = new File(projectTarget, String.format("%s-1.0-SNAPSHOT-flume-plugin.tar.gz", pluginName));
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
        for (String jarFile : getDependencies("flume-hdfs-sink")) {
            assertThat(new File(libExtDirectory, jarFile)).exists();
        }

        // Verify that the sink JAR, itself, was not copied into the libext directory
        assertThat(new File(libExtDirectory, libFilename)).doesNotExist();
    }
}
