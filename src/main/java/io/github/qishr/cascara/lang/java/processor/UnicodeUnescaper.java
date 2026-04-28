package io.github.qishr.cascara.lang.java.processor;


/// JLS 3.3 compliant Unicode un-escaper
/// https://docs.oracle.com/javase/specs/jls/se17/html/jls-3.html
public class UnicodeUnescaper {

    /**
     * Parses a string containing literal JLS-style Unicode escape sequences (e.g., \u0041 or \uuuu0041)
     * and replaces them with the corresponding Unicode characters.
     * Sequences that are malformed or incomplete are treated as literal characters.
     *
     * @param input The string with potential Unicode escapes (e.g., read from a file).
     * @return The unescaped string.
     */
    public static String unescapeUnicode(String input) {
        if (input == null) {
            return null;
        }

        StringBuilder output = new StringBuilder(input.length());
        int len = input.length();
        int i = 0;

        while (i < len) {
            char ch = input.charAt(i);

            // Check for potential start of a Unicode escape: `\\u`
            if (ch == '\\' && i + 1 < len && input.charAt(i + 1) == 'u') {

                int startHex = i + 2;

                // JLS 3.3 allows multiple 'u's (e.g., \uuuu0041). Consume them all.
                while (startHex < len && input.charAt(startHex) == 'u') {
                    startHex++;
                }

                // A valid escape must be followed by exactly 4 hex digits.
                if (startHex + 4 <= len) {
                    try {
                        String hex = input.substring(startHex, startHex + 4);

                        // Parse the four hex digits into an integer (the code point)
                        int codePoint = Integer.parseInt(hex, 16);

                        // Append the character corresponding to the code point
                        output.append((char) codePoint);

                        // Move the index past the entire escape sequence
                        i = startHex + 4;
                        continue; // Skip the default append and move to the next character
                    } catch (NumberFormatException e) {
                        // The 4 characters were not valid hex digits.
                        // Fall-through to the default action: treat the '\' literally.
                    }
                }
            }

            // Default action: Append the current character and move to the next one
            output.append(ch);
            i++;
        }

        return output.toString();
    }

    public static void main(String[] args) {
        System.out.println("--- JLS Unicode Unescaper Examples ---");

        // Example 1: Basic escape (\u006F is 'o')
        String escapedText1 = "Hello, w\\u006Frld!";
        System.out.println("Input: " + escapedText1);
        System.out.println("Output: " + unescapeUnicode(escapedText1));
        System.out.println("------------------------------------");

        // Example 2: Multiple escapes and multiple 'u's (\u0041 is 'A')
        String escapedText2 = "\\u0041\\uuu0042\\u0043"; // ABC
        System.out.println("Input: " + escapedText2);
        System.out.println("Output: " + unescapeUnicode(escapedText2));
        System.out.println("------------------------------------");

        // Example 3: Non-ASCII character (\u2603 is a Snowman ☃)
        String escapedText3 = "The weather is: \\u2603";
        System.out.println("Input: " + escapedText3);
        System.out.println("Output: " + unescapeUnicode(escapedText3));
        System.out.println("------------------------------------");

        // Example 4: Malformed/Incomplete escapes
        String escapedText4 = "Malformed 1: \\u004Z9 | Incomplete: \\u123";
        System.out.println("Input: " + escapedText4);
        System.out.println("Output: " + unescapeUnicode(escapedText4));
        // The parser treats the malformed/incomplete sequence as literal text in this case.

        // Example 5: Supplementary character (Watermelon 🍉 - U+1F349)
        // It is correctly represented by a surrogate pair: \uD83C (high) \uDF49 (low)
        String escapedText5 = "Watermelon: \\uD83C\\uDF49";
        System.out.println("Input: " + escapedText5);
        String unescaped5 = unescapeUnicode(escapedText5);
        System.out.println("Output: " + unescaped5);
        System.out.println("Output length (should be 12 + 2 chars): " + unescaped5.length());
        System.out.println("Code Point Count (should be 12 + 1 char): " + unescaped5.codePointCount(0, unescaped5.length()));
        System.out.println("------------------------------------");
    }
}
