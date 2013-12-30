package com.github.jrh3k5.flume.mojo.plugin;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.shared.invoker.InvocationResult;
import org.junit.Test;

import com.github.jrh3k5.flume.mojo.plugin.io.ArchiveUtils;

public class BuildDependencyPluginMojoITest extends AbstractFlumePluginMojoITest {
    @Test
    public void testBuildDependencyFlumePlugin() throws Exception {
        final String projectName = "flume-hdfs-sink-test-project";
        final InvocationResult result = buildProject(projectName, LifecyclePhase.PACKAGE);
        System.out.println(FileUtils.readFileToString(getLogFile()));
        assertThat(result.getExitCode()).isZero();

        final File projectTarget = new File(getProjectDirectory(projectName), "target");
        final File pluginFile = new File(projectTarget, "flume-hdfs-sink-1.0-SNAPSHOT-flume-plugin.tar.gz");
        assertThat(pluginFile).exists();

        final File testDirectory = getTestDirectory();
        final File tarFile = new File(testDirectory, "flume-hdfs-sink-1.0-SNAPSHOT-flume-plugin.tar");
        final File untarredDirectory = new File(testDirectory, "untarred");

        final ArchiveUtils archiveUtils = getArchiveUtils();
        archiveUtils.gunzipFile(pluginFile, tarFile);
        archiveUtils.untarFile(tarFile, untarredDirectory);

        final File pluginDirectory = new File(untarredDirectory, "flume-hdfs-sink");
        assertThat(pluginDirectory).exists();

        final File libDirectory = new File(pluginDirectory, "lib");
        assertThat(libDirectory).exists();
        assertThat(new File(libDirectory, "flume-hdfs-sink-1.4.0.jar")).exists();

        final File libExtDirectory = new File(pluginDirectory, "libext");
        assertThat(libExtDirectory).exists();
        for (String jarFile : Arrays.asList("commons-io-2.1.jar", "commons-lang-2.5.jar", "flume-ng-configuration-1.4.0.jar", "flume-ng-core-1.4.0.jar", "flume-ng-sdk-1.4.0.jar", "guava-10.0.1.jar",
                "slf4j-api-1.6.1.jar")) {
            assertThat(new File(libExtDirectory, jarFile)).exists();
        }
    }
}
