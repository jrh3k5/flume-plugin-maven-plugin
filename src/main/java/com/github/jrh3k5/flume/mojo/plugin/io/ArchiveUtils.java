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
package com.github.jrh3k5.flume.mojo.plugin.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.archiver.tar.TarArchiver;
import org.codehaus.plexus.archiver.tar.TarUnArchiver;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;

/**
 * Utilities for managing archives.
 * 
 * @author Joshua Hyde
 */

public class ArchiveUtils {
    private final Logger logger;

    /**
     * Create a archive utility.
     * 
     * @param logger
     *            A {@link Logger} used to log out the details of the archive
     *            actions.
     * @return An instance of this class.
     */
    public static ArchiveUtils getInstance(Logger logger) {
        return new ArchiveUtils(logger);
    }

    /**
     * Construct an instance of the archive utilities.
     * 
     * @param plexusLogger
     *            The {@link Logger} to be used to log activities of archiving.
     * @see #getInstance(Logger)
     */
    private ArchiveUtils(Logger plexusLogger) {
        if (plexusLogger == null) {
            throw new IllegalArgumentException("Logger cannot be null.");
        }
        this.logger = plexusLogger;
    }

    /**
     * Un-GZIP a file.
     * 
     * @param toUnzip
     *            A {@link File} representing the GZIP file to be unzipped.
     * @param toFile
     *            A {@link File} representing the location to which the unzipped
     *            file should be placed.
     * @throws IOException
     *             If any errors occur during the unzipping.
     * @see #gzipFile(File, File)
     */
    public void gunzipFile(File toUnzip, File toFile) throws IOException {
        if (!toUnzip.isFile()) {
            throw new IllegalArgumentException("Source file " + toUnzip + " must be an existent file.");
        }

        if (toFile.exists() && !toFile.isFile()) {
            throw new IllegalArgumentException("Destination file " + toFile + " exists, but is not a file and, as such, cannot be written to.");
        }

        GZIPInputStream zipIn = null;
        FileOutputStream fileOut = null;
        try {
            zipIn = new GZIPInputStream(new FileInputStream(toUnzip));
            fileOut = new FileOutputStream(toFile);
            IOUtils.copy(zipIn, fileOut);
        } finally {
            IOUtils.closeQuietly(fileOut);
            IOUtils.closeQuietly(zipIn);
        }
    }

    /**
     * GZIP a file.
     * 
     * @param toZip
     *            A {@link File} representing the file to be GZIP'ed.
     * @param toFile
     *            A {@link File} representing the location at which the GZIP
     *            file is to be created.
     * @throws IllegalArgumentException
     *             If the given source file is not a file or does not exist, or
     *             if the given destination file exists but is not a file.
     * @throws IOException
     *             If any errors occur during the GZIP'ing.
     * @see #gunzipFile(File, File)
     */
    public void gzipFile(File toZip, File toFile) throws IOException {
        if (!toZip.isFile()) {
            throw new IllegalArgumentException("Source file " + toZip + " must be an existent file.");
        }

        if (toFile.exists() && !toFile.isFile()) {
            throw new IllegalArgumentException("Destination file " + toFile + " exists, but is not a file and, as such, cannot be written to.");
        }

        GZIPOutputStream zipOut = null;
        FileInputStream tarIn = null;
        try {
            tarIn = new FileInputStream(toZip);
            zipOut = new GZIPOutputStream(new FileOutputStream(toFile));
            IOUtils.copy(tarIn, zipOut);
        } finally {
            IOUtils.closeQuietly(zipOut);
            IOUtils.closeQuietly(tarIn);
        }
    }

    /**
     * Store the contents of a directory in a TAR file.
     * 
     * @param directory
     *            A {@link File} representing the directory to be archived into
     *            a TAR file.
     * @param toFile
     *            A {@link File} representing the location at which the TAR file
     *            is to be created.
     * @throws IllegalArgumentException
     *             If the given source directory is not an existent directory or
     *             the given output file cannot be written to.
     * @throws IOException
     *             If any errors occur during the TAR'ing.
     * @see #untarFile(File, File)
     */
    public void tarDirectory(File directory, File toFile) throws IOException {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Source directory " + directory + " must be an existent directory.");
        }

        if (toFile.exists() && !toFile.isFile()) {
            throw new IllegalArgumentException("Destination file " + toFile + " exists, but is not a file and, as such, cannot be overwritten.");
        }

        final TarArchiver archiver = new TarArchiver();
        archiver.enableLogging(logger);
        archiver.setDestFile(toFile);
        archiver.addDirectory(directory);
        archiver.createArchive();
    }

    /**
     * Extract the contents of a TAR file.
     * 
     * @param tarFile
     *            A {@link File} representing the TAR file whose contents are to
     *            be extracted.
     * @param toDirectory
     *            A {@link File} representing the directory to which the
     *            contents of the TAR file to be extracted.
     * @throws IllegalArgumentException
     *             If the given TAR file is not a file or does not exist or the
     *             given output directory is not a directory or does not exist.
     * @throws IOException
     *             If any errors occur during the extraction.
     * @see #tarDirectory(File, File)
     */
    public void untarFile(File tarFile, File toDirectory) throws IOException {
        if (!tarFile.isFile()) {
            throw new IllegalArgumentException("TAR file " + tarFile + " must be an existent file.");
        }

        FileUtils.forceMkdir(toDirectory);

        final TarUnArchiver unarchiver = new TarUnArchiver(tarFile);
        unarchiver.enableLogging(logger);
        unarchiver.setDestDirectory(toDirectory);
        unarchiver.extract();
    }
}
