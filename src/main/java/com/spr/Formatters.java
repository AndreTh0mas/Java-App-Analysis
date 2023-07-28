package com.spr;

import jdk.jfr.consumer.RecordedMethod;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Formatters {
    public static String formatMethod(RecordedMethod m) {
        StringBuilder sb = new StringBuilder();
        String typeName = m.getType().getName(); // Returns full type Name
//        typeName = typeName.substring(typeName.lastIndexOf('.') + 1);
        sb.append(typeName).append(".").append(m.getName());
        sb.append("(");
        StringJoiner sj = new StringJoiner(", ");
        String md = m.getDescriptor().replace("/", ".");
        String parameter = md.substring(1, md.lastIndexOf(")"));
        for (String qualifiedName : decodeDescriptors(parameter)) {
            sj.add(qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1));
        }
        sb.append(sj.length() > 15 ? "..." : sj);
        sb.append(")");
        return sb.toString();
    }

    public static List<String> decodeDescriptors(String descriptor) {
        List<String> descriptors = new ArrayList<>();
        for (int index = 0; index < descriptor.length(); index++) {
            String arrayBrackets = "";
            while (descriptor.charAt(index) == '[') {
                arrayBrackets += "[]";
                index++;
            }

            String type = "<unknown-descriptor-type>";

            switch (descriptor.charAt(index)) {
                case 'L': {
                    int endIndex = descriptor.indexOf(';', index);
                    String s = descriptor.substring(index + 1, endIndex);
                    index = endIndex;
                    type = s;
                    break;
                }
                case 'I': {
                    type = "int";
                    break;
                }
                case 'J': {
                    type = "long";
                    break;
                }
                case 'Z' : {
                    type = "boolean";
                    break;
                }
                case 'D' : {
                    type = "double";
                    break;
                }
                case 'F' : {
                    type = "float";
                    break;
                }
                case 'S' : {
                    type = "short";
                    break;
                }
                case 'C' : {
                    type = "char";
                    break;
                }
                case 'B' : {
                    type = "byte";
                    break;
                }
            };
            descriptors.add(type + arrayBrackets);
        }
        return descriptors;
    }

    public static String formatBytes(Number value) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        if (value == null || value.longValue()<=0) {
            return "N/A";
        }
        double bytes = value.doubleValue();
        if (bytes >= 1024 * 1024 * 1024) {
            return decimalFormat.format(bytes / (1024 * 1024 * 1024)) + " GB";
        }
        if (bytes >= 1024 * 1024) {
            return decimalFormat.format(bytes / (1024 * 1024 )) + " MB";
        }
        if (bytes >= 1024) {
            return decimalFormat.format(bytes / 1024) + " kB";
        }
        return decimalFormat.format(bytes) + " bytes";
    }

    public static String formatPercentage(Number value) {
        if (value == null || value.longValue() == -1) {
            return "N/A";
        }
        return String.format("%6.2f %%", value.doubleValue() * 100);
    }
    enum TimespanUnit {
        NANOSECONDS("ns", 1000), MICROSECONDS("us", 1000), MILLISECONDS("ms", 1000),
        SECONDS("s", 60), MINUTES("m", 60), HOURS("h", 24), DAYS("d", 7);

        final String text;
        final long amount;
        TimespanUnit(String unit, long amount) {
            this.text = unit;
            this.amount = amount;
        }
    }
    public static String formatDuration(Number value) {
        if (value == null || value.longValue()<=0) {
            return "N/A";
        }
        double t = value.doubleValue();
        TimespanUnit result = TimespanUnit.NANOSECONDS;
        for (TimespanUnit unit : TimespanUnit.values()) {
            result = unit;
            if (t < 1000) {
                break;
            }
            t = t / unit.amount;
        }
        return String.format("%.1f %s", t, result.text);
    }


    public static void writeParam(StringBuilder template, String variable, String value) { // Where the variable is $NAME
        int lastIndex = 0;

        int index = template.indexOf(variable, lastIndex);
        if (index == -1) {
            return;
        }
        if (value == null || value.isEmpty() ) {
            value = "N/A";
        }
        if(value.length()>55){
            value = value.substring(0,55);
            value+="..";
        }
        int length = Math.max(value.length(), variable.length());
        for (int i = 0; i < length; i++) {
            char c = i < value.length() ? value.charAt(i) : ' ';
            template.setCharAt(index + i, c);
        }
    }

    public static int parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            System.out.println("Not a valid PID value: " + value);
            System.exit(0);
        }
        return 0;
    }

}
