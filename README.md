# dg-crisis-downloader
Download manager for Digital Globe Open Data (Crisis) imagery.

This was quickly hacked together to support the Hurricane Irma event.  The 
JavaFX interface isn't stitched to the controller yet so the CLI interface 
is the only way to kick it off (see MainApp).  I'll clean this up after the 
event.

# Build/Runtime Dependencies
- [Java 1.8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [Maven](http://maven.apache.org) (build-time, not runtime)

# CLI Execution
Currently CLI execution is the only way to use this tool.  The CLI requires 
two arguments, -u and -d, the others are optional:

| Short Arg | Long Arg |       Description                         |
|:---------:|:--------:|-------------------------------------------|               
| -u        |  --url   | DigitalGlobe Crisis Page URL              |
| -d        |  --dest  | Destination folder                        |
| -t        |--threads | Maximum number of threads (default 5)     |

For example the following command will download all DigitalGlobe pre-event 
imagery for Hurricane Irma into the folder `Z:\imagery\irma\pre`:
`java -jar dg-crisis-downloader-0.0.1-SNAPSHOT.jar -u "https://www.digitalglobe.com/opendata/hurricane-irma/pre-event" -d "Z:\imagery\irma\pre"`

# Concurrency (Simultaneous downloads)
Setting maximum number of threads available to the `DownloadManager` is 
currently the only way to regulate the number of simultaneous file downloads 
from the server.  The default number of threads is `5`, and you can set it to 
whatever you want...but be kind.  DigitalGlobe is providing this data for free, 
and there are other organizations that need this data too, so don't beat 
up their server.
