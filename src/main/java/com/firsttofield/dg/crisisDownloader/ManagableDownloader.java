/*
 * Copyright 2017 Your Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.firsttofield.dg.crisisDownloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Downloads a remote resource (URL) to a local file providing basic management
 * and monitoring capabilities.
 *
 * @author Steve Siebert
 */
public class ManagableDownloader implements Runnable {

    private final URL source;
    private final File destination;
    private final boolean overwrite;
    private long fileSize; //size of source file (-1 if unknown)
    private volatile long downloadSize; //number of bytes downloaded
    private volatile DownloadStatus status;

    private static final Logger LOGGER
            = Logger.getLogger(ManagableDownloader.class.getName());

    public ManagableDownloader(URL source, File destination, boolean overwrite) {
        this.source = source;
        this.destination = destination;
        this.status = DownloadStatus.PENDING;
        this.fileSize = -1;
        this.overwrite = overwrite;
    }

    @Override
    public void run() throws IllegalStateException {
        if (status != DownloadStatus.PENDING) {
            final String error = String.format("Cannot download '%s', "
                    + "file is currenly %s.", source.toString(), status);
            LOGGER.log(Level.WARNING, error);
            throw new IllegalStateException(error);
        }

        try {
            URLConnection conn = source.openConnection();
            conn.setUseCaches(Boolean.TRUE);
            conn.setAllowUserInteraction(Boolean.FALSE);
            fileSize = conn.getContentLengthLong();

            if (destination.exists()) {
                if (!overwrite) {
                    status = DownloadStatus.CANCELED;
                    LOGGER.log(Level.FINE, String.format("Skipping download '%s', "
                            + "file already exists and overwrite is not permitted.",
                            source.toString()));
                    return;
                } else {
                    LOGGER.log(Level.FINE, String.format("Overwritting "
                            + "file '%s'.", destination.getAbsolutePath()));
                }
            }

            status = DownloadStatus.DOWNLOADING;
            
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, String.format("Downloading '%s' to '%s'.", 
                        source.toString(), destination.getAbsolutePath()));
            }
            
            try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
                    BufferedOutputStream out = new BufferedOutputStream(
                            new FileOutputStream(destination, Boolean.FALSE))) {
                final byte[] buffer = new byte[4098];
                int read = 0;
                while (status != DownloadStatus.CANCELED
                        && (read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    downloadSize += read;
                }
            }
            status = DownloadStatus.COMPLETE;

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Downloaded '%s' to '%s'. COMPLETE.'",
                        source.toString(), destination.getAbsolutePath()));
            }
        } catch (IOException ex) {
            status = DownloadStatus.ERROR;
            LOGGER.log(Level.SEVERE,
                    String.format("Unable to download '%s': %s",
                            source.toString(), ex.getMessage()),
                    ex);
        }

    }

    public void cancel() {
        if (status == DownloadStatus.PENDING
                || status == DownloadStatus.DOWNLOADING) {
            status = DownloadStatus.CANCELED;
        }
    }

    public DownloadStatus getStatus() {
        return this.status;
    }

    public float getProgress() {
        if (fileSize == -1) {
            return fileSize;
        } else if (fileSize == 0) {
            return 0;
        }
        return downloadSize / fileSize;

    }

    @Override
    public String toString() {
        return String.format("[%s] %s", this.status.name(), source.toString());
    }
}
