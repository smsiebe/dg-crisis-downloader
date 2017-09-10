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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 *
 * @author Steve Siebert
 */
public class DownloadManager {

    private final Collection<ManagableDownloader> downloads;
    private final Executor executor;
    private final boolean overwrite;
    private File destinationDir;

    private static final boolean DEFAULT_OVERWRITE = Boolean.FALSE;

    public DownloadManager() {
        this(Executors.newCachedThreadPool(),
                new File(System.getProperty("java.io.tmpdir")),
                DEFAULT_OVERWRITE);
    }

    public DownloadManager(int numThreads, File destinationDir) {
        this(Executors.newFixedThreadPool(numThreads),
                destinationDir, DEFAULT_OVERWRITE);
    }

    public DownloadManager(Executor executor, File destinationDir, boolean overwrite) {

        this.executor = executor;
        this.destinationDir = destinationDir;
        this.downloads = new ArrayList<>();
        this.overwrite = overwrite;
    }

    public void download(URL source) {
        this.download(source, this.overwrite);
    }

    public void download(URL source, boolean overwrite) {
        ManagableDownloader download = new ManagableDownloader(source,
                new File(destinationDir, getFileName(source)),
                overwrite);
        downloads.add(download);
        executor.execute(download);
    }

    public File getDestinationDir() {
        return destinationDir;
    }

    public void clearDone() {
        Iterator<ManagableDownloader> iterator = this.downloads.iterator();
        while (iterator.hasNext()) {
            ManagableDownloader d = iterator.next();
            if (d.getStatus() != DownloadStatus.PENDING
                    || d.getStatus() != DownloadStatus.DOWNLOADING) {
                iterator.remove();
            }
        }
    }

    /**
     * Clears the downloaded files, returning the download objects.
     *
     * @return downloads completed
     */
    public Collection<ManagableDownloader> getDownloadedAndClear() {
        Collection<ManagableDownloader> complete = downloads.stream()
                .filter(this::isComplete)
                .collect(Collectors.toList());
        downloads.removeAll(complete);
        return complete;
    }

    public Collection<ManagableDownloader> getDownloads() {
        return new ArrayList<>(this.downloads); //defensive copy
    }

    private String getFileName(URL url) {
        String[] parts = url.getFile().split("/");
        return String.join("_", Arrays.copyOfRange(parts, 1, parts.length));
    }

    /**
     * Returns true if there are any downloads PENDING or DOWNLOADING.
     *
     * @return
     */
    public boolean isDownloading() {
        return this.downloads.stream()
                .anyMatch(this::isComplete);
    }

    /**
     *
     * @return number of tracked downloads (in any status)
     */
    public int getTotalManaged() {
        return this.downloads.size();
    }

    /**
     *
     * @return number of downloads PENDING or DOWNLOADING
     */
    public int getTotalDownloading() {
        return (int) this.downloads.stream()
                .filter(this::isComplete)
                .count();
    }

    private boolean isComplete(ManagableDownloader d) {
        return !(d.getStatus().equals(DownloadStatus.PENDING)
                || d.getStatus().equals(DownloadStatus.DOWNLOADING));
    }
}
