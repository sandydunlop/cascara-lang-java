package io.github.qishr.cascara.lang.java.processor;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utility class for splitting strings based on JLS 3.4 Line Terminators.
 * @see https://docs.oracle.com/javase/specs/jls/se17/html/jls-3.html#jls-3.4
 */
public class LineSplitter {

    /**
     * Divides the input string into a list of strings, split by any JLS-compliant
     * Line Terminator (LF, CR, or CRLF). The resulting list elements do not
     * contain the line terminator characters.
     * * @param input The string to be split.
     * @return A List of strings, where each element represents a line.
     */
    public static List<String> splitByJlsLineTerminators(String input) {
        if (input == null) {
            return null;
        }

        // --- Option 1: Modern (Java 11+) and Recommended ---
        // The String.lines() method is specifically designed to handle all
        // JLS-compliant line terminators and is the cleanest approach.
        return input.lines().collect(Collectors.toList());

        /*
        // --- Option 2: Regex-based (For older Java versions or explicit regex requirement) ---
        // The regex "\\r\\n|\\r|\\n" correctly matches CRLF as a single delimiter,
        // or CR/LF individually. The String.split() method automatically handles empty lines
        // and returns lines without the delimiter.
        return Arrays.asList(input.split("\\r\\n|\\r|\\n"));
        */
    }

    // --- Example Usage ---
    public static void main(String[] args) {
        // Test string containing all three JLS line terminators
        String testString = "Line 1\nLine 2\r\nLine 3\rLine 4";

        System.out.println("Input String:\n" + testString.replace("\n", "\\n").replace("\r", "\\r"));

        List<String> lines = splitByJlsLineTerminators(testString);

        System.out.println("\nSplit Lines:");
        for (int i = 0; i < lines.size(); i++) {
            System.out.println("Line " + (i + 1) + ": \"" + lines.get(i) + "\"");
        }

        // Output should be:
        // Line 1: "Line 1"
        // Line 2: "Line 2"
        // Line 3: "Line 3"
        // Line 4: "Line 4"
    }
}