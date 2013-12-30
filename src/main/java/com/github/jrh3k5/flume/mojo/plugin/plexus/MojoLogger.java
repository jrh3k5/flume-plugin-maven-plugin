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
package com.github.jrh3k5.flume.mojo.plugin.plexus;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;

/**
 * A {@link Logger} that delegates to a Maven mojo {@link Log}.
 * 
 * @author Joshua Hyde
 */

public class MojoLogger extends AbstractLogger {
    private final Log log;

    /**
     * Determine a {@link Logger} level based on the configuration of the given
     * {@link Log}.
     * 
     * @param log
     *            The {@link Log} to determine the logger level.
     * @return A {@link Logger} level corresponding to the log level in the
     *         given {@link Log}.
     */
    private static int determineThreshold(Log log) {
        if (log.isDebugEnabled()) {
            return Logger.LEVEL_DEBUG;
        } else if (log.isInfoEnabled()) {
            return Logger.LEVEL_INFO;
        } else if (log.isWarnEnabled()) {
            return Logger.LEVEL_WARN;
        } else if (log.isErrorEnabled()) {
            return Logger.LEVEL_ERROR;
        } else {
            return Logger.LEVEL_DISABLED;
        }
    }

    /**
     * Create a logger.
     * 
     * @param log
     *            The {@link Log} to which this logger will delegate.
     * @param mojoClass
     *            The {@link Class} of the mojo that owns this logger.
     */
    public MojoLogger(Log log, Class<?> mojoClass) {
        this(log, mojoClass.getSimpleName());
    }

    /**
     * Create a logger.
     * 
     * @param log
     *            The {@link Log} to which this logger will delegate.
     * @param name
     *            The name of the logger.
     */
    public MojoLogger(Log log, String name) {
        super(determineThreshold(log), name);
        this.log = log;
    }

    /**
     * {@inheritDoc}
     */
    public void debug(String message, Throwable throwable) {
        log.debug(message, throwable);
    }

    /**
     * {@inheritDoc}
     */
    public void info(String message, Throwable throwable) {
        log.info(message, throwable);
    }

    /**
     * {@inheritDoc}
     */
    public void warn(String message, Throwable throwable) {
        log.warn(message, throwable);
    }

    /**
     * {@inheritDoc}
     */
    public void error(String message, Throwable throwable) {
        log.error(message, throwable);
    }

    /**
     * {@inheritDoc}
     */
    public void fatalError(String message, Throwable throwable) {
        log.error(message, throwable);
    }

    /**
     * {@inheritDoc}
     */
    public Logger getChildLogger(String name) {
        return new MojoLogger(log, name);
    }
}
