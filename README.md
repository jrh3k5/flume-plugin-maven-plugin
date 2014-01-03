# flume-plugin-maven-plugin

A Maven plugin to generate archives that can be used as plugins in a Flume agent.

## Dependency Information

This plugin is available in the Maven 2 central repo. You can add it to your plugin simply by adding the following to your POM:

    <plugin>
        <groupId>com.github.jrh3k5</groupId>
        <artifactId>flume-plugin-maven-plugin</artifactId>
        <version>1.0</version> <!-- or latest version -->
    </plugin>

You can find the latest version released here:

http://central.maven.org/maven2/com/github/jrh3k5/flume-plugin-maven-plugin/

## Plugin Archive Format

This plugin assembles a <tt>.tar.gz</tt> with a <tt>lib/</tt> and <tt>libext/</tt> beneath a folder named for the given plugin name as part of the configuration (read below).

For example, if you've configured the plugin to create a plugin name "my-plugin" for the library "my-lib.jar" which depends on "a.jar" and "b.jar", the contents of the created <tt>.tar.gz</tt> file will be:

    my-project-1.0-my-plugin-flume-plugin.tar.gz
      |
      +- my-plugin
          |
          +- lib/
          |   |
          |   +- my-lib.jar
          +- libext/
              |
              +- a.jar
              +- b.jar

## Goals

This plugin provides the following goals.

### Assembly Goals

The following goals are used for building assemblies.

#### Goals

Below are the actual plugin goals themselves.

##### build-dependency-plugin

This plugin takes a specified dependency and assembles that assembly and its runtime and compile dependencies into a <tt>.tar.gz</tt> file that matches the structure of a Flume plugin.

An example configuration of this might look like:

    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>
        <groupId>com.github.jrh3k5</groupId>
        <artifactId>test-project</artifactId>
        <build>
            <plugins>
                <plugin>
                    <groupId>com.github.jrh3k5</groupId>
                    <artifactId>flume-plugin-maven-plugin</artifactId>
                    <executions>
                        <execution>
                            <id>build-hdfs-sink-plugin</id>
                            <goals>
                                <goal>build-dependency-plugin</goal>
                            </goals>
                            <configuration>
                                <dependency>
                                    <groupId>org.apache.flume.flume-ng-sinks</groupId>
                                    <artifactId>flume-hdfs-sink</artifactId>
                                </dependency>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
        <dependencies>
            <dependency>
                <groupId>org.apache.flume.flume-ng-sinks</groupId>
                <artifactId>flume-hdfs-sink</artifactId>
                <version>1.4.0</version>
            </dependency>
        </dependencies>
    </project>

This will assemble a <tt>.tar.gz</tt> in your project's <tt>target</tt> directory called <tt>test-project-1.0-SNAPSHOT-flume-hdfs-sink-flume-plugin.tar.gz</tt> and attach it to your project for its deployment.

By default, the plugin name is the name of the dependency being packaged.

##### build-project-plugin

This plugin assembles your current project, rather than a dependency of it, and its dependencies into a <tt>.tar.gz</tt> archive that matches the structure of a Flume plugin. An example usage might look like:

    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>
        <groupId>com.github.jrh3k5</groupId>
        <artifactId>test-project</artifactId>
        <version>1.0-SNAPSHOT</version>
        <build>
            <plugins>
                <plugin>
                    <groupId>com.github.jrh3k5</groupId>
                    <artifactId>flume-plugin-maven-plugin</artifactId>
                    <executions>
                        <execution>
                            <id>build-project-plugin</id>
                            <goals>
                                <goal>build-project-plugin</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
        <dependencies>
            <dependency>
                <groupId>org.apache.flume.flume-ng-sinks</groupId>
                <artifactId>flume-hdfs-sink</artifactId>
            </dependency>
            <dependency>
                <groupId>junit</groupId>
                <artifactId>junit</artifactId>
                <scope>test</scope>
            </dependency>
        </dependencies>
    </project>

This will create an artifact called <tt>test-project-1.0-SNAPSHOT-flume-plugin.tar.gz</tt> and attach it to your project. It will contain the JAR produced by this project in the <tt>lib/</tt> folder of the plugin and all of its runtime and compile dependencies in the <tt>libext/</tt> directory. Take note that, even though JUnit is listed as a dependency of this project, it will be excluded because it is a <tt>test</tt>-scoped dependency.

By default, the plugin name will be the same as the project artifact ID (and is thus omitted from the final artifact name to avoid redundant naming).

#### Shared Configuration

The following goals share also the following configuration elements:

##### Metainformation Configuration

You can change the classifier suffix, plugin name (by default, inherited from the artifact ID of the project using the plugin), and whether or not the artifact is attached with the following configuration elements:

    <configuration>
        <!-- Turn off attaching the artifact to your project -->
        <attach>false</attach>
        <!-- Change the classifier suffix from "flume-plugin" -->
        <classifierSuffix>different-classifier-suffix</classifierSuffix>
        <!-- Change the plugin name -->
        <pluginName>not-the-artifactId</pluginName>
    </configuration>

##### Exclude Artifacts from Assembly

You may wish to not package some artifacts with your plugin. You can exclude an artifact from the final assembly with the following configuration:

    <configuration>
        <exclusions>
            <exclusion>
                <groupId>org.slf4j</groupId>
                <artifactId>slf4j-log4j12</artifactId>
            </exclusion>
        </exclusions>
    </configuration>

The above example will filter the <tt>slf4j-log4j12</tt> artifact from the plugin assembly.
