package com.github.jrh3k5.flume.mojo.plugin;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.StringUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Exclusion}.
 * 
 * @author Joshua Hyde
 * @since 1.1
 */

public class ExclusionTest {
    private final String groupId = UUID.randomUUID().toString();
    private final String artifactId = UUID.randomUUID().toString();
    private final Exclusion exclusion = new Exclusion();

    /**
     * Set up the exclusion for each test.
     */
    @Before
    public void setUp() {
        exclusion.setGroupId(groupId);
        exclusion.setArtifactId(artifactId);
    }

    /**
     * Test that matching on the fields of the artifact works.
     */
    @Test
    public void testMatches() {
        final Artifact artifact = mock(Artifact.class);
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        assertThat(exclusion.matches(artifact)).isTrue();
    }

    /**
     * If a classifier is specified, then the exclusion should match on it.
     */
    @Test
    public void testMatchesWithClassifier() {
        final String classifier = UUID.randomUUID().toString();
        exclusion.setClassifier(classifier);

        final Artifact artifact = mock(Artifact.class);
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getClassifier()).thenReturn(classifier);

        assertThat(exclusion.matches(artifact)).isTrue();
    }

    /**
     * If the group ID is different, then the exclusion should not match.
     */
    @Test
    public void testMatchesDifferentArtifactId() {
        final Artifact artifact = mock(Artifact.class);
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(StringUtils.reverse(artifactId));
        assertThat(exclusion.matches(artifact)).isFalse();
    }

    /**
     * If a classifier is specified, then the exclusion should match on it.
     */
    @Test
    public void testMatchesDifferentClassifier() {
        final String classifier = UUID.randomUUID().toString();
        exclusion.setClassifier(classifier);

        final Artifact artifact = mock(Artifact.class);
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getClassifier()).thenReturn(StringUtils.reverse(classifier));

        assertThat(exclusion.matches(artifact)).isFalse();
    }

    /**
     * If the group ID is different, then the exclusion should not match.
     */
    @Test
    public void testMatchesDifferentGroupId() {
        final Artifact artifact = mock(Artifact.class);
        when(artifact.getGroupId()).thenReturn(StringUtils.reverse(groupId));
        when(artifact.getArtifactId()).thenReturn(artifactId);
        assertThat(exclusion.matches(artifact)).isFalse();
    }
}
