package com.mahcode;

import java.util.Collections;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Random;

public class Main {
    
    public static Map<String, List<Integer>> markerData = new HashMap<>();
    public static List<String> lines = new ArrayList<>();
    public static List<Integer> stack = new ArrayList<>();
    public static boolean insideMarker = false;
    public static Scanner scanner = new Scanner(System.in);
    public static Random random = new Random();
    public static boolean running = true;
    
    public static void main(String[] args) {
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader("program.sdapl"));
            String line;
            
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            
            reader.close();
            
            for (int i = 0; i < lines.size(); i++) {
                if (!running) break;
                if (isInsideMarker(i)) {
                    continue;
                }
                
                String str = lines.get(i);
                String[] parts = str.split(" ", 2);
                String command = parts[0];
                String arg = parts.length > 1 ? parts[1] : "";
                
                if (!executeCommand(command, arg, stack, i)) {
                    scanner.close();
                    return;
                }
            }
            System.out.println("");
            scanner.close();
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            scanner.close();
        }
    }
    
    private static boolean isInsideMarker(int lineNumber) {
        for (List<Integer> marker : markerData.values()) {
            int start = marker.get(0);
            int length = marker.get(1);
            if (lineNumber > start && lineNumber <= start + length) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean executeCommand(String command, String arg, List<Integer> stack, int lineNumber) {
        if (command.startsWith("#"))
            return true;
        
		if (command.isEmpty()) {
			return true;
		}
		
        String cmd = command;
        if (!cmd.equals("(") && !cmd.equals(")")) {
            cmd = cmd.toUpperCase();
        }
        
        switch (cmd) {
            case "(":
                if (insideMarker) {
                    System.out.println("Error (line " + (lineNumber+1) + "): Can't create marker inside another marker");
                    return false;
                }
                if (!arg.equals("")) {
                    boolean found = false;
                    for (int j = lineNumber + 1; j < lines.size(); j++) {
                        if (lines.get(j).trim().equals(")")) {
                            List<Integer> tempData = new ArrayList<>();
                            tempData.add(lineNumber);
                            tempData.add(j - lineNumber);
                            markerData.put(arg, tempData);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        System.out.println("Error (line " + (lineNumber+1) + "): Marker didn't end");
                        return false;
                    }
                } else {
                    System.out.println("Error (line " + (lineNumber+1) + "): You haven't set a name for a marker");
                    return false;
                }
                break;
                
            case ")":
                break;
            
            case "RUN":
                if (markerData.containsKey(arg)) {
                    List<Integer> marker = markerData.get(arg);
                    int start = marker.get(0);
                    int length = marker.get(1);
                    
                    insideMarker = true;
                    for (int j = start + 1; j < start + length; j++) {
                        String runStr = lines.get(j);
                        String[] runParts = runStr.split(" ", 2);
                        String runCmd = runParts[0];
                        String runArg = runParts.length > 1 ? runParts[1] : "";
                        
                        if (!executeCommand(runCmd, runArg, stack, j)) {
                            insideMarker = false;
                            return false;
                        }
                    }
                    insideMarker = false;
                } else {
                    System.out.println("Error (line " + (lineNumber+1) + "): Marker '" + arg + "' not found");
                    return false;
                }
                break;
            
            case "NOTEMPTY":
                String[] markers = arg.split(" ");
                if (markers.length == 2) {
                    String m1 = markers[0];
                    String m2 = markers[1];
                    
                    if (stack.size() > 0) {
                        int last = stack.get(stack.size() - 1);
                        if (last != 0) {
                            if (markerData.containsKey(m1)) {
                                List<Integer> marker = markerData.get(m1);
                                int start = marker.get(0);
                                int length = marker.get(1);
                                
                                insideMarker = true;
                                for (int j = start + 1; j < start + length; j++) {
                                    String runStr = lines.get(j);
                                    String[] runParts = runStr.split(" ", 2);
                                    String runCmd = runParts[0];
                                    String runArg = runParts.length > 1 ? runParts[1] : "";
                                    
                                    if (!executeCommand(runCmd, runArg, stack, j)) {
                                        insideMarker = false;
                                        return false;
                                    }
                                }
                                insideMarker = false;
                            } else {
                                System.out.println("Error (line " + (lineNumber+1) + "): Marker '" + m1 + "' not found");
                                return false;
                            }
                        } else {
                            if (markerData.containsKey(m2)) {
                                List<Integer> marker = markerData.get(m2);
                                int start = marker.get(0);
                                int length = marker.get(1);
                                
                                insideMarker = true;
                                for (int j = start + 1; j < start + length; j++) {
                                    String runStr = lines.get(j);
                                    String[] runParts = runStr.split(" ", 2);
                                    String runCmd = runParts[0];
                                    String runArg = runParts.length > 1 ? runParts[1] : "";
                                    
                                    if (!executeCommand(runCmd, runArg, stack, j)) {
                                        insideMarker = false;
                                        return false;
                                    }
                                }
                                insideMarker = false;
                            } else {
                                System.out.println("Error (line " + (lineNumber+1) + "): Marker '" + m2 + "' not found");
                                return false;
                            }
                        }
                    } else {
                        System.out.println("Error (line " + (lineNumber+1) + "): Stack is empty");
                        return false;
                    }
                } else {
                    System.out.println("Error (line " + (lineNumber+1) + "): NOTEMPTY requires two marker names");
                    return false;
                }
                break;
            
            case "IF":
                String[] ifParts = arg.split(" ");
                if (ifParts.length == 5) {
                    try {
                        int pos1 = Integer.parseInt(ifParts[0]);
                        String op = ifParts[1].toUpperCase();
                        int pos2 = Integer.parseInt(ifParts[2]);
                        String trueMarker = ifParts[3];
                        String falseMarker = ifParts[4];
                        
                        if (pos1 < 0 || pos1 >= stack.size() || pos2 < 0 || pos2 >= stack.size()) {
                            System.out.println("Error (line " + (lineNumber+1) + "): Position out of bounds");
                            return false;
                        }
                        
                        int val1 = stack.get(stack.size() - 1 - pos1);
                        int val2 = stack.get(stack.size() - 1 - pos2);
                        boolean condition = false;
                        
                        switch (op) {
                            case "GREATER": condition = val1 > val2; break;
                            case "LESS": condition = val1 < val2; break;
                            case "EQUALS": condition = val1 == val2; break;
                            default: 
                                System.out.println("Error (line " + (lineNumber+1) + "): Unknown operator");
                                return false;
                        }
                        
                        String markerToRun = condition ? trueMarker : falseMarker;
                        if (markerData.containsKey(markerToRun)) {
                            List<Integer> marker = markerData.get(markerToRun);
                            int start = marker.get(0);
                            int length = marker.get(1);
                            
                            insideMarker = true;
                            for (int j = start + 1; j < start + length; j++) {
                                String runStr = lines.get(j);
                                String[] runParts = runStr.split(" ", 2);
                                String runCmd = runParts[0];
                                String runArg = runParts.length > 1 ? runParts[1] : "";
                                
                                if (!executeCommand(runCmd, runArg, stack, j)) {
                                    insideMarker = false;
                                    return false;
                                }
                            }
                            insideMarker = false;
                        } else {
                            System.out.println("Error (line " + (lineNumber+1) + "): Marker '" + markerToRun + "' not found");
                            return false;
                        }
                        
                    } catch (NumberFormatException e) {
                        System.out.println("Error (line " + (lineNumber+1) + "): IF requires numbers for positions");
                        return false;
                    }
                } else {
                    System.out.println("Error (line " + (lineNumber+1) + "): IF requires 5 arguments");
                    return false;
                }
                break;
            
            case "GET":
                String[] getArgs = arg.split(" ", 2);
                String mode = getArgs.length > 0 ? getArgs[0].toUpperCase() : "";
                String prompt = getArgs.length > 1 ? getArgs[1] : "Enter: ";
                
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                
                switch (mode) {
                    case "INT":
                        try {
                            int number = Integer.parseInt(input);
                            stack.add(number);
                        } catch (NumberFormatException e) {
                            System.out.println("Error (line " + (lineNumber+1) + "): You must enter a valid integer");
                            return false;
                        }
                        break;
                        
                    case "ASCII":
                        if (input.length() == 1) {
                            stack.add((int) input.charAt(0));
                        } else {
                            System.out.println("Error (line " + (lineNumber+1) + "): Enter a single character for ASCII mode");
                            return false;
                        }
                        break;
                        
                    case "STRING":
                        for (int j = input.length() - 1; j >= 0; j--) {
                            stack.add((int) input.charAt(j));
                        }
                        break;
                        
                    default: // Просто GET без режима
                        try {
                            int number = Integer.parseInt(input);
                            stack.add(number);
                        } catch (NumberFormatException e) {
                            if (input.length() == 1) {
                                stack.add((int) input.charAt(0));
                            } else {
                                System.out.println("Error (line " + (lineNumber+1) + "): Enter a number or single character");
                                return false;
                            }
                        }
                }
                break;
            
            case "ASCIIFY":
                if (!arg.equals("")) {
                    for (int j = arg.length() - 1; j >= 0; j--) {
                        stack.add((int) arg.charAt(j));
                    }
                } else {
                    System.out.println("Error (line " + (lineNumber+1) + "): ASCIIFY requires a string");
                    return false;
                }
                break;
            
            case "INSERT":
                String[] insertArgs = arg.split(" ", 2);
                if (insertArgs.length == 2) {
                    try {
                        int position = Integer.parseInt(insertArgs[0]);
                        String valueStr = insertArgs[1];
                        
                        if (position < 0 || position > stack.size()) {
                            System.out.println("Error (line " + (lineNumber+1) + "): Invalid position");
                            return false;
                        }
                        
                        int valueToInsert;
                        try {
                            valueToInsert = Integer.parseInt(valueStr);
                        } catch (NumberFormatException e) {
                            if (valueStr.length() == 1) {
                                valueToInsert = (int) valueStr.charAt(0);
                            } else {
                                System.out.println("Error (line " + (lineNumber+1) + "): Second argument must be a number or single character");
                                return false;
                            }
                        }
                        
                        int insertIndex = stack.size() - position;
                        stack.add(insertIndex, valueToInsert);
                        
                    } catch (NumberFormatException e) {
                        System.out.println("Error (line " + (lineNumber+1) + "): First argument must be a number");
                        return false;
                    }
                } else {
                    System.out.println("Error (line " + (lineNumber+1) + "): INSERT requires two arguments");
                    return false;
                }
                break;
            
            case "LENGTH":
                stack.add(stack.size());
                break;
            
            case "COPYFROM":
                try {
                    int position = Integer.parseInt(arg);
                    if (position < 0 || position >= stack.size()) {
                        System.out.println("Error (line " + (lineNumber+1) + "): Position out of bounds");
                        return false;
                    }
                    int index = stack.size() - 1 - position;
                    stack.add(stack.get(index));
                } catch (NumberFormatException e) {
                    System.out.println("Error (line " + (lineNumber+1) + "): COPYFROM requires a number");
                    return false;
                }
                break;
            
            case "POP":
                if (stack.size() > 0) {
                    stack.remove(stack.size() - 1);
                } else {
                    System.out.println("Error (line " + (lineNumber+1) + "): Stack is empty");
                    return false;
                }
                break;
            
            case "REMOVE":
                try {
                    int position = Integer.parseInt(arg);
                    if (position < 0 || position >= stack.size()) {
                        System.out.println("Error (line " + (lineNumber+1) + "): Position out of bounds");
                        return false;
                    }
                    int index = stack.size() - 1 - position;
                    stack.remove(index);
                } catch (NumberFormatException e) {
                    System.out.println("Error (line " + (lineNumber+1) + "): REMOVE requires a number");
                    return false;
                }
                break;
            
            case "STOP":
                running = false;
                break;
            
            case "DUMP":
                System.out.print("Stack: ");
                for (int j = 0; j < stack.size(); j++) {
                    System.out.print(stack.get(j));
                    if (j < stack.size() - 1) System.out.print(", ");
                }
                System.out.println();
                break;
            
            case "WELCOME":
                if (arg.equalsIgnoreCase("RANDOM")) {
                    stack.add(random.nextInt());
                } else if (!arg.matches(".*\\D.*")) {
                    stack.add(Integer.parseInt(arg));
                } else {
                    if (arg.length() < 2) {
                        stack.add((int) arg.charAt(0));
                    } else {
                        System.out.println("Error (line " + (lineNumber+1) + "): You can't welcome more than 1 letter at a time");
                        return false;
                    }
                }
                break;
            
            case "INTRODUCE":
                if (stack.size() >= 1) {
                    int value = stack.get(stack.size() - 1);
                    if (arg.equals("#")) {
                        System.out.print(value);
                    } else {
                        System.out.print((char) value);
                    }
                    stack.remove(stack.size() - 1);
                } else {
                    System.out.println("Error (line " + (lineNumber+1) + "): No one to introduce");
                    return false;
                }
                break;
            
            case "CLONE":
                stack.add(stack.get(stack.size() - 1));
                break;
            
            case "REVERSE":
                Collections.reverse(stack);
                break;
            
            case "ADD":
                int a1 = stack.get(stack.size() - 1);
                int a2 = stack.get(stack.size() - 2);
                stack.remove(stack.size() - 1);
                stack.remove(stack.size() - 1);
                stack.add(a2 + a1);
                break;
            
            case "SUB":
                int s1 = stack.get(stack.size() - 1);
                int s2 = stack.get(stack.size() - 2);
                stack.remove(stack.size() - 1);
                stack.remove(stack.size() - 1);
                stack.add(s2 - s1);
                break;
            
            case "MUL":
                int m1 = stack.get(stack.size() - 1);
                int m2 = stack.get(stack.size() - 2);
                stack.remove(stack.size() - 1);
                stack.remove(stack.size() - 1);
                stack.add(m2 * m1);
                break;
            
            case "DIV":
                int d1 = stack.get(stack.size() - 1);
                int d2 = stack.get(stack.size() - 2);
                stack.remove(stack.size() - 1);
                stack.remove(stack.size() - 1);
                stack.add(d2 / d1);
                break;
            
            case "MOD":
                int md1 = stack.get(stack.size() - 1);
                int md2 = stack.get(stack.size() - 2);
                stack.remove(stack.size() - 1);
                stack.remove(stack.size() - 1);
                stack.add(md2 % md1);
                break;
            
            case "SWAP":
                int sw1 = stack.get(stack.size() - 1);
                int sw2 = stack.get(stack.size() - 2);
                stack.remove(stack.size() - 1);
                stack.remove(stack.size() - 1);
                stack.add(sw1);
                stack.add(sw2);
                break;
            
            default:
                System.out.println("Error (line " + (lineNumber+1) + "): Command does not exist");
                return false;
        }
        return true;
    }
}