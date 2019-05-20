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
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Steve Siebert
 */
public class DownloadManagerTest {

    private static final String TEST_FILENAME = "irma_pre.html";

   // @Test
    public void testDownload() {
        DownloadManager mgr = new DownloadManager();
        mgr.download(DownloadManagerTest.class.getClassLoader().getResource(TEST_FILENAME));
        Collection<ManagableDownloader> downloads = mgr.getDownloads();
        downloads.forEach(
                (d) -> {
                    assertNotEquals(DownloadStatus.PENDING, d.getStatus());
                    while (d.getStatus() == DownloadStatus.PENDING) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ex) {
                        }
                        assertEquals(DownloadStatus.COMPLETE, d.getStatus());
                        assertEquals(1f, d.getProgress(), 0f);
                    }
                });
    }

    @Test
    public void testGetDestinationDir() {
        DownloadManager mgr = new DownloadManager();
        assertEquals(new File(System.getProperty("java.io.tmpdir")).getAbsolutePath(),
                mgr.getDestinationDir().getAbsolutePath());
    }

    @Test
    public void testClearDone() {
        DownloadManager mgr = new DownloadManager();
        mgr.download(DownloadManagerTest.class.getClassLoader().getResource(TEST_FILENAME));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
        }
        assertEquals(1, mgr.getDownloads().size());
        mgr.clearDone();
        assertEquals(0, mgr.getDownloads().size());
    }

}
