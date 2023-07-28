package com.spr;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedFrame;
import jdk.jfr.consumer.RecordingFile;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SprinklrMethodStats {
    private final HashMap<String,HashMap<String,Float>> HotMethodWiseSprinklrCOM = new HashMap<>(); // Represents for each HotMethod which sprinklr method is responsible
    private final HashMap<String,Float> HotSprinklrMethods= new HashMap<>(); // Overall Sprinklr methods % from all the samples.
    private static long TotalExecutionEvent = 0;
    HashMap<String,Float> Top5MethodSet; // Top5 Hot Methods from Application Statistics.
    public SprinklrMethodStats( HashMap<String,Float> Top5MethodSet){
        this.Top5MethodSet = Top5MethodSet;
    }
    private static final String SprinklrMethodTemplate =
            "            | $METHOD_NAME                                                      $FX_PE   |\n" +
            "            |        --------------------------------------------------------            |\n" +
            "            | $HOT_METHOD_COM_SPRINKLR                                          $JX_PE   |\n" +
            "            | $HOT_METHOD_COM_SPRINKLR                                          $JX_PE   |\n" +
            "            | $HOT_METHOD_COM_SPRINKLR                                          $JX_PE   |\n" +
            "            | $HOT_METHOD_COM_SPRINKLR                                          $JX_PE   |\n" +
            "            | $HOT_METHOD_COM_SPRINKLR                                          $JX_PE   |\n" +
            "            ==============================================================================";
    private static final String SprinklrTopMethodTemplate =
            "            |--------------------------Top Sprinklr Methods------------------------------|\n" +
            "            | $HOT_TOP_COM_SPRINKLR                                             $KX_PE   |\n" +
            "            | $HOT_TOP_COM_SPRINKLR                                             $KX_PE   |\n" +
            "            | $HOT_TOP_COM_SPRINKLR                                             $KX_PE   |\n" +
            "            | $HOT_TOP_COM_SPRINKLR                                             $KX_PE   |\n" +
            "            | $HOT_TOP_COM_SPRINKLR                                             $KX_PE   |\n" +
            "            ==============================================================================";
    public void printSprinklrStats(){
        List<Map.Entry<String, Float>> sortedEntriesHotMethods = Top5MethodSet.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                .collect(Collectors.toList());
        String variable = "N/A";
        String value = "N/A";
        boolean Printed = false;
        if(!sortedEntriesHotMethods.isEmpty()){
            for( Map.Entry<String,Float> entry: sortedEntriesHotMethods){
                if(!HotMethodWiseSprinklrCOM.containsKey(entry.getKey())) continue;
                List<Map.Entry<String, Float>> sortedOnSprinklrMethod = HotMethodWiseSprinklrCOM.get(entry.getKey()).entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                        .collect(Collectors.toList());
                StringBuilder MethodTemplate = new StringBuilder(SprinklrMethodTemplate);
                variable = "$METHOD_NAME";
                value = entry.getKey();
                Formatters.writeParam(MethodTemplate,variable,value);
                variable = "$FX_PE";
                value = Formatters.formatPercentage(entry.getValue()/TotalExecutionEvent);
                Formatters.writeParam(MethodTemplate,variable,value);
                for(int i = 0;i<5;i++){
                    variable = "$HOT_METHOD_COM_SPRINKLR";
                    value = "N/A";
                    if(i<sortedOnSprinklrMethod.size()) {
                        Map.Entry<String, Float> Entry = sortedOnSprinklrMethod.get(i);
                        value = Entry.getKey();
                    }
                    Formatters.writeParam(MethodTemplate,variable,value);
                    variable = "$JX_PE";
                    value = "N/A";
                    if(i<sortedOnSprinklrMethod.size()) {
                        Map.Entry<String, Float> Entry = sortedOnSprinklrMethod.get(i);
                        value = Formatters.formatPercentage(Entry.getValue()/entry.getValue());
                    }
                    Formatters.writeParam(MethodTemplate,variable,value);
                }
                if(!Printed){
                    Printed = true;
                    System.out.println("            |================ Sprinklr Methods Contributing to Hot Methods ==============|\n");
                }
                System.out.println(MethodTemplate);
            }
        }
        List<Map.Entry<String, Float>> sortedEntriesTopSprinklr = HotSprinklrMethods.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                .collect(Collectors.toList());

        StringBuilder TopSprinklrTemplate = new StringBuilder(SprinklrTopMethodTemplate);
        boolean isAvailable = false;
        for(int i = 0;i<5;i++){
            variable = "$HOT_TOP_COM_SPRINKLR";
            value = "N/A";
            if(i<sortedEntriesTopSprinklr.size()) {
                isAvailable = true;
                Map.Entry<String, Float> entry = sortedEntriesTopSprinklr.get(i);
                value = entry.getKey();
            }
            Formatters.writeParam(TopSprinklrTemplate,variable,value);
            variable = "$KX_PE";
            value = "N/A";
            if(i<sortedEntriesTopSprinklr.size()) {
                Map.Entry<String, Float> entry = sortedEntriesTopSprinklr.get(i);
                value = Formatters.formatPercentage(entry.getValue()/TotalExecutionEvent);
            }
            Formatters.writeParam(TopSprinklrTemplate,variable,value);
        }
        if(isAvailable)
            System.out.println(TopSprinklrTemplate);
    }
    public void Runner(Path file){
        try (RecordingFile recordingFile = new RecordingFile(file)){ // Re-Reading the file
            while (recordingFile.hasMoreEvents()){
                RecordedEvent e = recordingFile.readEvent();
                String EventName = e.getEventType().getName();
                if(EventName.equals("jdk.ExecutionSample")) {
                    try {
                        List<RecordedFrame> frames = e.getStackTrace().getFrames(); // getting all the frames from the Execution event.
                        if (!frames.isEmpty()) {
                            RecordedFrame topFrame = frames.get(0);
                            if(topFrame.isJavaFrame()) {
                                String CurrentFrame;
                                TotalExecutionEvent++;
                                String topMethod = Formatters.formatMethod(topFrame.getMethod());
                                for (RecordedFrame frame : frames) {
                                    CurrentFrame = Formatters.formatMethod(frame.getMethod());
                                    if(CurrentFrame.startsWith("com.spr")){
                                        HotSprinklrMethods.merge(CurrentFrame,(float)1,Float::sum);
                                        if(Top5MethodSet.containsKey(topMethod)){
                                            if(HotMethodWiseSprinklrCOM.containsKey(topMethod)){
                                                HotMethodWiseSprinklrCOM.get(topMethod).merge(CurrentFrame,(float)1,Float::sum);
                                            }
                                            else{
                                                HotMethodWiseSprinklrCOM.put(topMethod,new HashMap<>());
                                                HotMethodWiseSprinklrCOM.get(topMethod).put(CurrentFrame,(float)1);
                                            }
                                        }
                                        break; // As we have found the top most Sprinklr Method on the stack
                                    }
                                }
                            }
                        }
                    } catch (Exception exception) {
                        // Do Nothing : Null pointer Exception -> most common
                    }
                }
            }
            printSprinklrStats();
        } catch (IOException e) {
            System.out.println("Not able to read the recording. Maybe it was deleted or moved.");
            e.printStackTrace();
        }
    }
}
