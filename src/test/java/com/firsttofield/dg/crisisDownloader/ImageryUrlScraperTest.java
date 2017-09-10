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

import java.net.URL;
import java.util.Collection;
import org.junit.Assert;

/**
 *
 * @author Steve Siebert
 */
public class ImageryUrlScraperTest {
    
    private static final String TEST_FILENAME = "irma_pre.html";

    
    @org.junit.Test
    public void testCall() throws Exception {
        
        URL url = ImageryUrlScraperTest.class.getClassLoader().getResource(TEST_FILENAME);
        ImageryUrlScraper scraper = new ImageryUrlScraper(url);
        Collection<URL> imageryLinks = scraper.call();
        Assert.assertEquals(598, imageryLinks.size());
    }
    
}
