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
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");

        stage.setTitle("DigitalGlobe Crisis Imagery Downloader");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            launch(args);
        } else {
            cli(args);
        }
    }

    private static void cli(String... args) throws Exception {
        Options options = new Options();
        options.addOption(Option.builder("u")
                .longOpt("url")
                .hasArg()
                .argName("url")
                .required()
                .desc("DigitalGlobe Crisis Page URL")
                .build());
        options.addOption(Option.builder("d")
                .hasArg()
                .argName("folder")
                .required()
                .longOpt("dest")
                .desc("Destination folder")
                .build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        //TODO ugly implementation quickly done for this crisis
        File dest = new File(cmd.getOptionValue("d"));
        if (!dest.exists()) {
            dest.mkdirs();
        }
        final DownloadManager mgr = new DownloadManager(5, dest);

        ImageryUrlScraper scraper = new ImageryUrlScraper(
                new URL(cmd.getOptionValue("u")));
        scraper.call().forEach((u) -> {
            mgr.download(u);
        });

        while (mgr.isDownloading()) {
            mgr.getDownloadedAndClear().forEach((d) -> System.out.println(
                    String.format("Complete: %s", d)));
            System.out.println(String.format("%d downloads remaining.", mgr.getTotalDownloading()));
            Thread.sleep(TimeUnit.SECONDS.toMillis(10));
        }
    }

}
