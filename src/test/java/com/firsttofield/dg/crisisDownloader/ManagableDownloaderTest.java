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
import java.io.IOException;
import java.net.URL;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author smsie
 */
public class ManagableDownloaderTest {

    private static final String TEST_FILENAME = "irma_pre.html";

    @Test
    public void testRun() {

        URL url = ManagableDownloaderTest.class.getClassLoader().getResource(TEST_FILENAME);
        File tmpDest = null;
        try {
            tmpDest = File.createTempFile("downloader", "test");
        } catch (IOException ex) {
            fail("Unable to create temp file.");
        }

        ManagableDownloader download = new ManagableDownloader(url, tmpDest, true);
        download.run(); //executed synchronously
        assertEquals(486, 630, tmpDest.length());
    }

    @Test(expected = IllegalStateException.class)
    public void testCancel() {
        URL url = ManagableDownloaderTest.class.getClassLoader().getResource(TEST_FILENAME);
        File tmpDest = null;
        try {
            tmpDest = File.createTempFile("downloader", "test");
        } catch (IOException ex) {
            fail("Unable to create temp file.");
        }
        ManagableDownloader download = new ManagableDownloader(url, tmpDest, true);

        assertEquals(DownloadStatus.PENDING, download.getStatus());
        download.cancel();
        assertEquals(DownloadStatus.CANCELED, download.getStatus());
        download.run(); //expected to throw IllegalStateException
    }

    @Test
    public void testGetProgress() {
        URL url = ManagableDownloaderTest.class.getClassLoader().getResource(TEST_FILENAME);
        File tmpDest = null;
        try {
            tmpDest = File.createTempFile("downloader", "test");
        } catch (IOException ex) {
            fail("Unable to create temp file.");
        }
        ManagableDownloader download = new ManagableDownloader(url, tmpDest, true);

        assertEquals(-1f, download.getProgress(), 0f); //we don't know the file size yet because it hasn't been executed
        download.run(); //executed synchronously
        assertEquals(1f, download.getProgress(), 0f);
    }

    @Test
    public void testOverwriteProtection () {
        URL url = ManagableDownloaderTest.class.getClassLoader().getResource(TEST_FILENAME);
        File tmpDest = null;
        try {
            tmpDest = File.createTempFile("downloader", "test");
        } catch (IOException ex) {
            fail("Unable to create temp file.");
        }
        ManagableDownloader download = new ManagableDownloader(url, tmpDest, true);

        assertEquals(-1f, download.getProgress(), 0f); //we don't know the file size yet because it hasn't been executed
        download.run(); //executed synchronously
        assertEquals(1f, download.getProgress(), 0f);
        
        //not attempt to download again
        ManagableDownloader overwriteProtected = new ManagableDownloader(url, tmpDest, false);
        
        assertEquals(-1f, overwriteProtected.getProgress(), 0f); //we don't know the file size yet because it hasn't been executed
        overwriteProtected.run(); //executed synchronously
        assertEquals(0.0f, overwriteProtected.getProgress(), 0f);
        assertEquals(DownloadStatus.CANCELED, overwriteProtected.getStatus());
    }
}
