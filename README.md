
# Java JFR Analyzer
Java JFR Analyzer is a Java application designed to read events from Java Flight Recorder (JFR) files and provide insightful analysis on various aspects of the application's performance. The application offers in-depth information, including Hotmethods, Allocation statistics, and more, to help developers and performance engineers optimize their Java applications without the need for additional applications like Java Mission Control.



## How to Use

To start using this project. Go to the `root directory` and run:

```bash
  ./gradlew build
```
To create a Jar file including all the dependencies run:

```bash
  ./gradlew shadowJar
```
This would create a Jar file named `Java-JFR-Analyzer-1.0-all.jar`.
To run the application use the command in the `root directory`:
```bash
  java -jar ./build/libs/Java-JFR-Analyzer-1.0-all.jar <PID|Path to ".jfr" File>
```
Provide PID of the process you want to analyze in which case it would Start a new flight recording upon user inputs such as duration and path to dump the JFR recording. Or provide path to the JFR file you want to analyze.

## Why Use the Java JFR Analysis Tool?
``Simplified Analysis``: With this tool, you can conveniently analyze JFR files without relying on external applications, making the process more straightforward and accessible.

``Comprehensive Performance Insights``: By exploring the Hotmethods and Allocation Statistics, developers can pinpoint performance issues and optimize the application's resource utilization.

``Sprinklr-Specific Analysis``: The integration of Sprinklr methods makes this tool particularly useful as teams won't have to go through Trees of stack trace to find the specific method.

``Time-Saving``: The user-friendly interface and straightforward functionality save time that would otherwise be spent navigating complex tools for JFR analysis such as JMC.


## Features

![App Screenshot](https://github.com/AndreTh0mas/Java-JFR-Analyzer/blob/e581e2c859e0599d55c5a096e93eda8677282bcc/OverviewScreenshot.png)

All the values in the first Tab: ``Applcation Statistics`` either represents maximum value or value avereged over the time span of the profiling session depending on the attribute. Example: for CPU Machine it represents maximum recorded over profiling time while Allocation Rate represent average over profiling time.

__Overall metrics__: Includes information such as Total allocatoin, Allocation rates, Max compilation time, Heap Analysis, CPU analysis on Machine and the JVM etc.  
Also includes ``Garbage Collection Health`` information for monitoring and assessing the health of our application's garbage collection process to ensure optimal memory reclamation.

__Hotmethods Analysis__: The tool identifies the hottest methods in the application, providing insights into performance bottlenecks and potential areas for optimization.

__Allocation Statistics__: Allocation behaviour of the application such as Top allocation methods & ``Sprinklr specific allocation methods``.

__High CPU Threads__: Threads taking most amount of CPU and Hot Methods associated with them.

![TLAB Allocation](https://github.com/AndreTh0mas/Java-JFR-Analyzer/blob/5b3c4ff4c3f6576c2e4a89f6b82171581edf15a4/AllocationThroughTLAB.png)

Depending on the Jdk version, sometimes instead of ``ObjectAllocation`` event, ``In and Outside TlAB`` events are fired to give the allocation statistics of the application. Hence i have added capabilities for both the scenarios. This TAB shows top allocating methods using TLAB events.



## Requirements

Java 11 or higher.
