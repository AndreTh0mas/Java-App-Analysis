package com.spr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class JFRRecordingThroughPID {
    private static void printStdOutput(Process proc) throws IOException {
        BufferedReader stdInput = new BufferedReader(
                new InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(
                new InputStreamReader(proc.getErrorStream()));

        // Read the output from the command
        System.out.println(
                "Here is the standard output of the command:\n");
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }
        // Read any errors from the attempted command
        System.out.println(
                "Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
    }
    public static Path RecordingStartUsingPID(long PID) throws Exception{
        Scanner scanner = new Scanner(System.in);
        System.out.println("Starting a new JFR recording");
        System.out.print("Specify the Duration of Recording in seconds (Press enter for default = 100s): ");
        String InputDuration = scanner.nextLine();
        int duration = 100;
        if (!InputDuration.trim().isEmpty()) {
            // Parse the user input to an integer if it's not empty
            try {
                duration = Integer.parseInt(InputDuration);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Using default duration of " + duration + " seconds.");
            }
        }
        String DurationInJFR = duration+"s";
        System.out.println("Specify the Path of Recording:Example: /Users/harsh.kumar/Desktop ,Press enter for default = This directory");
        String InputPath = scanner.nextLine();
        String currentPath = Paths.get("").toAbsolutePath() + "FlightRecorder.jfr";
        if(!InputPath.trim().isEmpty()){
            try{
                Path path = Paths.get(InputPath);
                System.out.println("The path is valid: "+path);
                currentPath = InputPath+"FlightRecorder.jfr";
            }
            catch(Exception ex){
                System.out.println("Invalid Path given: Using default path-> "+currentPath);
            }
        }
        // Starting the recording.
        Process p = Runtime.getRuntime().exec( //Executes the specified string command in a separate process
                "jcmd " + PID + " JFR.start "+ "duration="+DurationInJFR+" filename="+currentPath+" name=myrecording settings=profile");
        // p -> Process object to manage the subprocess just created.
        printStdOutput(p); // To output the standard output and Error of the process here.
        // Wait for jcmd to start the recording
        System.out.println("If you see JFR recording did not start: close the process and try again");
        Thread.sleep(1000);
        // Waiting for the recording to complete
        Thread.sleep((long)duration*1000 + 10000); // Waiting for 10 extra seconds than duration for everything to complete.
        return Paths.get(currentPath);
    }
}
