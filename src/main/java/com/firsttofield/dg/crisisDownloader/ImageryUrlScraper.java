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

import com.googlecode.streamflyer.core.Modifier;
import com.googlecode.streamflyer.core.ModifyingReader;
import com.googlecode.streamflyer.regex.MatchProcessorResult;
import com.googlecode.streamflyer.regex.RegexModifier;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Scrapes links to *.tif and *.tif.ovr files from the DigitalGlobe Open Data
 * event page (ie
 * https://www.digitalglobe.com/opendata/hurricane-irma/pre-event)
 *
 * @author Steve Siebert
 */
public class ImageryUrlScraper implements Callable<Collection<URL>> {

    private final URL openDataUrl;
    private static final String IMAGERY_LINK_REGEX
            = "href=\\\"(.*\\.tif(\\.ovr)?)\\\"";
    private static final Logger LOGGER
            = Logger.getLogger(ImageryUrlScraper.class.getName());

    public ImageryUrlScraper(URL openDataUrl) {
        this.openDataUrl = openDataUrl;
    }

    @Override
    public Collection<URL> call() throws IOException {

        final Collection<URL> imageryUrls = new HashSet<>();

        @SuppressWarnings("FinallyDiscardsException")
        Modifier sfModifier = new RegexModifier(IMAGERY_LINK_REGEX,
                Pattern.CASE_INSENSITIVE,
                (StringBuilder sb, int i, MatchResult mr) -> {
                    //Adds the matched URL to the provided collection
                    try {
                        imageryUrls.add(new URL(mr.group(1)));
                    } catch (MalformedURLException ex) {
                        LOGGER.log(Level.WARNING,
                                String.format("Problem while scraping imagery "
                                        + "URLs from '%s'; discoverd link (href) "
                                        + "'%s' was not a valid URL.",
                                        openDataUrl.toString(),
                                        mr.group())
                        );
                    } finally {
                        return new MatchProcessorResult(mr.end(), true);
                    }
                }, 0, 2048);

        try (BufferedReader reader = new BufferedReader(
                new ModifyingReader(
                        new InputStreamReader(openDataUrl.openStream()),
                        sfModifier))) {

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest(line);
                }
            }
            return imageryUrls;
        }
    }

}
