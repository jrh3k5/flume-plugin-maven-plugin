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
package com.github.jrh3k5.flume.mojo.plugin.artifact;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

import com.github.jrh3k5.flume.mojo.plugin.FlumePluginDependency;

/**
 * An {@link ArtifactFilter} that only qualifies artifacts that match the given {@link FlumePluginDependency}.
 * 
 * @author Joshua Hyde
 */

public class FlumePluginDependencyArtifactFilter implements ArtifactFilter {
    private final FlumePluginDependency dependency;

    /**
     * Create a filter.
     * 
     * @param dependency
     *            The {@link FlumePluginDependency} to drive matching of an artifact.
     */
    public FlumePluginDependencyArtifactFilter(FlumePluginDependency dependency) {
        this.dependency = dependency;
    }

    @Override
    public boolean include(Artifact artifact) {
        boolean matches = dependency.getGroupId().matches(artifact.getGroupId());
        matches &= dependency.getArtifactId().matches(artifact.getArtifactId());
        return matches;
    }

}
