package io.github.qishr.cascara.lang.java.processor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UnicodeUnescaperTests {

    // Helper constant for the method under test
    private static final java.util.function.Function<String, String> UNESCAPER =
        UnicodeUnescaper::unescapeUnicode;

    /**
     * Tests a basic Unicode escape sequence.
     * \u006F should unescape to 'o'.
     */
    @Test
    void testBasicEscape() {
        // Arrange
        String escapedText = "Hello, w\\u006Frld!";
        String expected = "Hello, world!";

        // Act & Assert
        assertEquals(expected, UNESCAPER.apply(escapedText));
    }

    /**
     * Tests multiple escapes and the JLS feature of allowing multiple 'u's.
     * \u0041 is 'A', \uuu0042 is 'B', \u0043 is 'C'.
     */
    @Test
    void testMultipleEscapesAndMultipleUs() {
        // Arrange
        String escapedText = "\\u0041\\uuu0042\\u0043";
        String expected = "ABC";

        // Act & Assert
        assertEquals(expected, UNESCAPER.apply(escapedText));
    }

    /**
     * Tests a non-ASCII character (Basic Multilingual Plane - BMP).
     * \u2603 should unescape to the Snowman character (☃).
     */
    @Test
    void testNonAsciiCharacter() {
        // Arrange
        String escapedText = "The weather is: \\u2603";
        // The literal '☃' is used in the expected string
        String expected = "The weather is: ☃";

        // Act & Assert
        assertEquals(expected, UNESCAPER.apply(escapedText));
    }

     /**
     * Tests malformed and incomplete escape sequences.
     * Per the logic, these sequences should be treated as literal text.
     * - \\u004Z9: 'Z' is not a hex digit. // The compiler now sees two literal characters: '\' and 'u'
     * - \\u123: is incomplete (needs 4 hex digits). // The compiler now sees two literal characters: '\' and 'u'
     */
    @Test
    void testMalformedAndIncompleteEscapes() {
        // Arrange
        String escapedText = "Malformed 1: \\u004Z9 | Incomplete: \\u123";
        // Since the parser treats them as literal, the expected output is the input string.
        String expected = "Malformed 1: \\u004Z9 | Incomplete: \\u123";

        // Act & Assert
        assertEquals(expected, UNESCAPER.apply(escapedText));
    }

    /**
     * Tests a supplementary character (Watermelon 🍉 - U+1F349) which is
     * correctly represented by a UTF-16 surrogate pair (\uD83C\uDF49).
     * The unescaper should process two separate 4-digit escapes, resulting in two char units.
     */
    @Test
    void testSupplementaryCharacterSurrogatePair() {
        // Arrange
        String escapedText = "Watermelon: \\uD83C\\uDF49";
        // The literal '🍉' is used in the expected string, which is internally a surrogate pair.
        String expected = "Watermelon: 🍉";

        // Act
        String unescaped = UNESCAPER.apply(escapedText);

        // Assert
        assertEquals(expected, unescaped, "The surrogate pair should unescape to the correct emoji.");

        // Assert the resulting string's properties:
        // Length should be 12 chars ("Watermelon: ") + 2 char units (the surrogate pair) = 14
        assertEquals(14, unescaped.length(), "The string length should include the two char units of the surrogate pair.");

        // Code point count should be 12 chars ("Watermelon: ") + 1 code point (the emoji) = 13
        assertEquals(13, unescaped.codePointCount(0, unescaped.length()), "The code point count should confirm the two char units form one code point.");
    }

    /**
     * Tests null input returns null.
     */
    @Test
    void testNullInput() {
        assertNull(UNESCAPER.apply(null));
    }

    /**
     * Tests an empty string input.
     */
    @Test
    void testEmptyInput() {
        assertEquals("", UNESCAPER.apply(""));
    }

    /**
     * Tests input with no escape sequences.
     */
    @Test
    void testNoEscapes() {
        String input = "Just a regular string.";
        assertEquals(input, UNESCAPER.apply(input));
    }
}

