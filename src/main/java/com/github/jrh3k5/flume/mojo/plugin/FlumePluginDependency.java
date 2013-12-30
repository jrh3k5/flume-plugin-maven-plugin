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

/**
 * Definition of the dependency to be resolved and bundled into a Flume plugin.
 * 
 * @author Joshua Hyde
 */

public class FlumePluginDependency {
    private String groupId;
    private String artifactId;

    /**
     * Get the group ID of the dependency to be bundled.
     * 
     * @return The group ID of the dependency to be bundled.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Set the group ID of the dependency to be bundled.
     * 
     * @param groupId
     *            The group ID of the dependency to be bundled.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Get the artifact ID to be resolved into a Flume plugin.
     * 
     * @return The artifact ID to be resolved into a Flume plugin.
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Set the artifact ID to be resolved into a Flume plugin.
     * 
     * @param artifactId
     *            The artifact ID to be resolved into a Flume plugin.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Get the formatted identifier of this dependency.
     * 
     * @return The formatted identifier of this dependency.
     */
    public String getFormattedIdentifier() {
        return String.format("%s:%s", getGroupId(), getArtifactId());
    }
}
