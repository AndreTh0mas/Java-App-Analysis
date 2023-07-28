package com.spr;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import static com.spr.JFRRecordingThroughPID.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class Main {
    private static void printHelp(){
        System.out.println("---------------------------USAGE--------------------------------");
        System.out.println("java ApplicationStatistics.java [options] <|pid|file>\n");
        System.out.println("--------------------------EXAMPLES------------------------------");
        System.out.println("java HealthReport.java 4711");
        System.out.println("java HealthReport.java PATH/recording.jfr");
        System.out.println("Example Path: /Users/harsh.kumar/Downloads/flight_recording-2.jfr\n");
        List<VirtualMachineDescriptor> vmDescriptors = VirtualMachine.list();
        System.out.println("--------------------Running Java Processes-----------------------");
        System.out.println("PID\t    DisplayName");
        System.out.println("---     -----------");
        boolean Count = false;
        for (VirtualMachineDescriptor vmDescriptor : vmDescriptors) {
            String pid = vmDescriptor.id();
            String displayName = vmDescriptor.displayName();
            System.out.println(pid + "\t" + displayName);
            Count = true;
        }
        if(!Count){
            System.out.println("Found no running Java processes");
        }
    }


    public static void main(String[] args){
        if(args.length == 0 || args[0].equals("-help")){
            printHelp();
            System.exit(0);
        }
        ApplicationStatistics ApplicationReport= new ApplicationStatistics();
        // Checking if the argument is a file
        if(args[0].length()>4){
            if(args[0].endsWith(".jfr")){
                try{
                    Path file = Path.of(args[0]);
                    // Run the runner
                    ApplicationReport.Runner(file);
                    System.exit(0);
                } catch (Exception ex){
                    System.out.println("Invalid path of the jfr file or the file does not exists");
                    System.exit(0);
                }
            }
        }

        // Not a JFR file
        // Have to check for The pid of the process and then going start a new JFR recording.
        if(Character.isDigit(args[0].charAt(0))){
            // Must be the PID of a Java process.
            long PID = Formatters.parseInteger(args[0]);
            Optional<ProcessHandle> processHandle = ProcessHandle.of(PID);
            // Check if the process is present (running)
            if (processHandle.isPresent()) {
                System.out.println("Java Process with PID: " + PID + " found.");
                try{
                    Path JfrFilepath = RecordingStartUsingPID(PID);
                    if(Files.exists(JfrFilepath)){
                        ApplicationReport.Runner(JfrFilepath);
                    }
                    else{
                        System.out.println("Jfr recording was unsuccessful please try again with proper arguments");
                    }
                }
                catch (Exception ex){
                    System.out.println("Something went wrong, Please try again");
                    ex.printStackTrace();
                    System.exit(0);
                }
            } else {
                System.out.println("Java Process with PID: " + PID + " not found.");
            }
        }
        else{
            System.out.println("Invalid arguments\n");
            printHelp();
        }
    }

}
