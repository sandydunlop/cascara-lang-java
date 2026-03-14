package io.github.qishr.cascara.lang.java.processor;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.qishr.cascara.common.diagnostic.Reporter;
import io.github.qishr.cascara.common.lang.LanguageOptions;
import io.github.qishr.cascara.common.lang.processor.Tokenizer;
import io.github.qishr.cascara.lang.java.JavaOptions;
import io.github.qishr.cascara.lang.java.token.JavaToken;
import io.github.qishr.cascara.lang.java.token.JavaTokenType;
import io.github.qishr.cascara.lang.java.token.SemanticType;

/**
 * A definitive tokenizer (lexer) for Java source code.
 * This version processes the source as a single string and handles line/column tracking internally,
 * ensuring newlines are correctly tokenized as part of WHITESPACE.
 */
public class JavaTokenizer implements Tokenizer<JavaToken> {
    JavaOptions options = new JavaOptions();
    Reporter reporter = null;
    private URI uri = null;

    private static final SemanticType UNKNOWN = SemanticType.NONE;
    /**
     * Defines the types of tokens recognized by the lexer.
     */

    private static final Map<String, JavaTokenType> KEYWORDS = new HashMap<>();
    static {
        // Reserved Keywords
        KEYWORDS.put("abstract", JavaTokenType.KEYWORD_ABSTRACT); KEYWORDS.put("assert", JavaTokenType.KEYWORD_ASSERT);
        KEYWORDS.put("boolean", JavaTokenType.KEYWORD_BOOLEAN); KEYWORDS.put("break", JavaTokenType.KEYWORD_BREAK);
        KEYWORDS.put("byte", JavaTokenType.KEYWORD_BYTE); KEYWORDS.put("case", JavaTokenType.KEYWORD_CASE);
        KEYWORDS.put("catch", JavaTokenType.KEYWORD_CATCH); KEYWORDS.put("char", JavaTokenType.KEYWORD_CHAR);
        KEYWORDS.put("class", JavaTokenType.KEYWORD_CLASS); KEYWORDS.put("const", JavaTokenType.KEYWORD_CONST);
        KEYWORDS.put("continue", JavaTokenType.KEYWORD_CONTINUE); KEYWORDS.put("default", JavaTokenType.KEYWORD_DEFAULT);
        KEYWORDS.put("do", JavaTokenType.KEYWORD_DO); KEYWORDS.put("double", JavaTokenType.KEYWORD_DOUBLE);
        KEYWORDS.put("else", JavaTokenType.KEYWORD_ELSE); KEYWORDS.put("enum", JavaTokenType.KEYWORD_ENUM);
        KEYWORDS.put("extends", JavaTokenType.KEYWORD_EXTENDS); KEYWORDS.put("final", JavaTokenType.KEYWORD_FINAL);
        KEYWORDS.put("finally", JavaTokenType.KEYWORD_FINALLY); KEYWORDS.put("float", JavaTokenType.KEYWORD_FLOAT);
        KEYWORDS.put("for", JavaTokenType.KEYWORD_FOR); KEYWORDS.put("goto", JavaTokenType.KEYWORD_GOTO);
        KEYWORDS.put("if", JavaTokenType.KEYWORD_IF); KEYWORDS.put("implements", JavaTokenType.KEYWORD_IMPLEMENTS);
        KEYWORDS.put("import", JavaTokenType.KEYWORD_IMPORT); KEYWORDS.put("instanceof", JavaTokenType.KEYWORD_INSTANCEOF);
        KEYWORDS.put("int", JavaTokenType.KEYWORD_INT); KEYWORDS.put("interface", JavaTokenType.KEYWORD_INTERFACE);
        KEYWORDS.put("long", JavaTokenType.KEYWORD_LONG); KEYWORDS.put("native", JavaTokenType.KEYWORD_NATIVE);
        KEYWORDS.put("new", JavaTokenType.KEYWORD_NEW); KEYWORDS.put("package", JavaTokenType.KEYWORD_PACKAGE);
        KEYWORDS.put("private", JavaTokenType.KEYWORD_PRIVATE); KEYWORDS.put("protected", JavaTokenType.KEYWORD_PROTECTED);
        KEYWORDS.put("public", JavaTokenType.KEYWORD_PUBLIC); KEYWORDS.put("return", JavaTokenType.KEYWORD_RETURN);
        KEYWORDS.put("short", JavaTokenType.KEYWORD_SHORT); KEYWORDS.put("static", JavaTokenType.KEYWORD_STATIC);
        KEYWORDS.put("strictfp", JavaTokenType.KEYWORD_STRICTFP); KEYWORDS.put("super", JavaTokenType.KEYWORD_SUPER);
        KEYWORDS.put("switch", JavaTokenType.KEYWORD_SWITCH); KEYWORDS.put("synchronized", JavaTokenType.KEYWORD_SYNCHRONIZED);
        KEYWORDS.put("this", JavaTokenType.KEYWORD_THIS); KEYWORDS.put("throw", JavaTokenType.KEYWORD_THROW);
        KEYWORDS.put("throws", JavaTokenType.KEYWORD_THROWS); KEYWORDS.put("transient", JavaTokenType.KEYWORD_TRANSIENT);
        KEYWORDS.put("try", JavaTokenType.KEYWORD_TRY); KEYWORDS.put("void", JavaTokenType.KEYWORD_VOID);
        KEYWORDS.put("volatile", JavaTokenType.KEYWORD_VOLATILE); KEYWORDS.put("while", JavaTokenType.KEYWORD_WHILE);
        KEYWORDS.put("open", JavaTokenType.KEYWORD_OPEN);

        // Contextual Keywords
        KEYWORDS.put("exports", JavaTokenType.CONTEXTUAL_EXPORTS); KEYWORDS.put("module", JavaTokenType.CONTEXTUAL_MODULE);
        KEYWORDS.put("non-sealed", JavaTokenType.CONTEXTUAL_NON_SEALED); KEYWORDS.put("opens", JavaTokenType.CONTEXTUAL_OPENS);
        KEYWORDS.put("permits", JavaTokenType.CONTEXTUAL_PERMITS); KEYWORDS.put("provides", JavaTokenType.CONTEXTUAL_PROVIDES);
        KEYWORDS.put("record", JavaTokenType.CONTEXTUAL_RECORD); KEYWORDS.put("requires", JavaTokenType.CONTEXTUAL_REQUIRES);
        KEYWORDS.put("sealed", JavaTokenType.CONTEXTUAL_SEALED); KEYWORDS.put("to", JavaTokenType.CONTEXTUAL_TO);
        KEYWORDS.put("transitive", JavaTokenType.CONTEXTUAL_TRANSITIVE); KEYWORDS.put("uses", JavaTokenType.CONTEXTUAL_USES);
        KEYWORDS.put("var", JavaTokenType.CONTEXTUAL_VAR); KEYWORDS.put("with", JavaTokenType.CONTEXTUAL_WITH);
        KEYWORDS.put("yield", JavaTokenType.CONTEXTUAL_YIELD);
        KEYWORDS.put("when", JavaTokenType.CONTEXTUAL_WHEN); // <-- ADDED 'when'

        // Literals
        KEYWORDS.put("true", JavaTokenType.BOOLEAN_LITERAL); KEYWORDS.put("false", JavaTokenType.BOOLEAN_LITERAL);
        KEYWORDS.put("null", JavaTokenType.KEYWORD_NULL);
    }

    private static class LexerState {
        boolean isInsideBlockComment = false;
        boolean isInsideTextBlock = false;
    }

    /**
     * Peeks at the character at the given offset from the current position.
     */
    private char peek(String source, int current, int offset) {
        if (current + offset >= source.length()) {
            return '\0';
        }
        return source.charAt(current + offset);
    }

    @Override
    public JavaTokenizer setReporter(Reporter reporter) {
        this.reporter = reporter;
        return this;
    }

    @Override
    public JavaTokenizer setOptions(LanguageOptions<?> options) {
        this.options = (JavaOptions)options;
        return this;
    }

    @Override
    public List<JavaToken> tokenize(String source) {
        return tokenize(source, null);
    }

    /// Entry point for the tokenization process.
    ///
    /// @param source The YAML text to process.
    /// @param uri The source URI for diagnostic reporting.
    /// @return A list of tokens including structural start/end markers.
    @Override
    public List<JavaToken> tokenize(String source, URI uri) {
        this.uri = uri;
        boolean verbose = options.getVerbose();
        boolean includeNonSemanticTokens = options.getIncludeComments();

        String sourceCode = UnicodeUnescaper.unescapeUnicode(source);
        List<JavaToken> tokens = new ArrayList<>();
        if (sourceCode == null || sourceCode.isEmpty()) {
            return tokens;
        }

        LexerState state = new LexerState();
        int current = 0;
        int lineNumber = 1;
        int column = 1;

        while (current < sourceCode.length()) {
            char c = sourceCode.charAt(current);
            int start = current;
            int startLine = lineNumber;
            int startCol = column;
            JavaToken token = null;

            // --- 1. Comment/Whitespace/TextBlock State Handling ---

            // BLOCK COMMENT
            if (state.isInsideBlockComment) {
                int endCommentIndex = sourceCode.indexOf("*/", current);
                if (endCommentIndex != -1) {
                    int endCommentEnd = endCommentIndex + 2;
                    String lexeme = sourceCode.substring(start, endCommentEnd);

                    // Update position for block comment content
                    for (int i = start; i < endCommentEnd; i++) {
                        char charAtI = sourceCode.charAt(i);
                        if (charAtI == '\n') { lineNumber++; column = 1; }
                        else if (charAtI == '\r') { /* Ignore \r if followed by \n */ }
                        else { column++; }
                    }

                    current = endCommentEnd;
                    state.isInsideBlockComment = false;
                    token = new JavaToken(JavaTokenType.BLOCK_COMMENT_CONTENT, lexeme, start, startLine, startCol, UNKNOWN);
                } else {
                    // Comment spans to EOF
                    String lexeme = sourceCode.substring(start);
                    for (int i = start; i < sourceCode.length(); i++) {
                         char charAtI = sourceCode.charAt(i);
                        if (charAtI == '\n') { lineNumber++; column = 1; }
                        else if (charAtI == '\r') { /* Ignore \r if followed by \n */ }
                        else { column++; }
                    }
                    current = sourceCode.length();
                    token = new JavaToken(JavaTokenType.BLOCK_COMMENT_CONTENT, lexeme, start, startLine, startCol, UNKNOWN);
                }
            }

            // WHITESPACE (Includes newlines)
            else if (Character.isWhitespace(c)) {
                // Consume all contiguous whitespace characters
                while (current < sourceCode.length() && Character.isWhitespace(sourceCode.charAt(current))) {
                    char nextC = sourceCode.charAt(current);

                    if (nextC == '\n') {
                        // Consumed \n
                        lineNumber++;
                        column = 1;
                    } else if (nextC == '\r') {
                        // Consumed \r. Check for \r\n sequence to avoid double counting
                        if (current + 1 < sourceCode.length() && sourceCode.charAt(current + 1) == '\n') {
                            // \r\n is a single line break
                            lineNumber++;
                            column = 1;
                        } else {
                            // \r by itself is a line break
                            lineNumber++;
                            column = 1;
                        }
                    } else {
                        // Space or Tab
                        column++;
                    }
                    current++;
                }

                String lexeme = sourceCode.substring(start, current);
                token = new JavaToken(JavaTokenType.WHITESPACE, lexeme, start, startLine, startCol, UNKNOWN);
            }

            // BLOCK COMMENT START
            else if (c == '/' && peek(sourceCode, current, 1) == '*') {
                current += 2;
                column += 2;
                state.isInsideBlockComment = true;
                continue; // Restart loop to handle comment content
            }

            // SINGLE-LINE COMMENT
            else if (c == '/' && peek(sourceCode, current, 1) == '/') {
                int endOfLine = sourceCode.indexOf('\n', current);
                if (endOfLine == -1) {
                    endOfLine = sourceCode.length();
                }

                String lexeme = sourceCode.substring(start, endOfLine);
                current = endOfLine;
                column += lexeme.length(); // Update column based on comment length
                // Note: Line/Column for next token will be updated by the WHITESPACE handling when it consumes the \n
                token = new JavaToken(JavaTokenType.SINGLE_LINE_COMMENT, lexeme, start, startLine, startCol, UNKNOWN);
            }

            // --- Conditional Filtering for Non-Semantic Tokens ---
            if (token != null && isNonSemantic(token.getType())) {
                if (!includeNonSemanticTokens) {
                    token = null; // Discard token for parser mode
                    continue; // Skip the token addition block
                }
            }

            // --- 2. Multi-character Operator Checks (Greedy Matching) ---
            if (token == null) {
                switch (c) {
                    case '=':
                        if (peek(sourceCode, current, 1) == '=') { current += 2; column += 2; token = new JavaToken(JavaTokenType.EQUALS_EQUALS, "==", start, startLine, startCol, UNKNOWN); }
                        else { current++; column++; token = new JavaToken(JavaTokenType.EQUALS, "=", start, startLine, startCol, UNKNOWN); }
                        break;
                    case '!':
                        if (peek(sourceCode, current, 1) == '=') { current += 2; column += 2; token = new JavaToken(JavaTokenType.BANG_EQUALS, "!=", start, startLine, startCol, UNKNOWN); }
                        else { current++; column++; token = new JavaToken(JavaTokenType.BANG, "!", start, startLine, startCol, UNKNOWN); }
                        break;
                    // ... (All other multi-character operators follow, ensuring current and column are updated)
                    case '-':
                        if (peek(sourceCode, current, 1) == '>') { current += 2; column += 2; token = new JavaToken(JavaTokenType.ARROW, "->", start, startLine, startCol, UNKNOWN); }
                        else if (peek(sourceCode, current, 1) == '=') { current += 2; column += 2; token = new JavaToken(JavaTokenType.MINUS_EQUALS, "-=", start, startLine, startCol, UNKNOWN); }
                        else if (peek(sourceCode, current, 1) == '-') { current += 2; column += 2; token = new JavaToken(JavaTokenType.MINUS_MINUS, "--", start, startLine, startCol, UNKNOWN); }
                        else { current++; column++; token = new JavaToken(JavaTokenType.MINUS, "-", start, startLine, startCol, UNKNOWN); }
                        break;
                    case '+':
                        if (peek(sourceCode, current, 1) == '=') { current += 2; column += 2; token = new JavaToken(JavaTokenType.PLUS_EQUALS, "+=", start, startLine, startCol, UNKNOWN); }
                        else if (peek(sourceCode, current, 1) == '+') { current += 2; column += 2; token = new JavaToken(JavaTokenType.PLUS_PLUS, "++", start, startLine, startCol, UNKNOWN); }
                        else { current++; column++; token = new JavaToken(JavaTokenType.PLUS, "+", start, startLine, startCol, UNKNOWN); }
                        break;
                    case '*':
                        if (peek(sourceCode, current, 1) == '=') { current += 2; column += 2; token = new JavaToken(JavaTokenType.STAR_EQUALS, "*=", start, startLine, startCol, UNKNOWN); }
                        else { current++; column++; token = new JavaToken(JavaTokenType.STAR, "*", start, startLine, startCol, UNKNOWN); }
                        break;
                    case '%':
                        if (peek(sourceCode, current, 1) == '=') { current += 2; column += 2; token = new JavaToken(JavaTokenType.PERCENT_EQUALS, "%=", start, startLine, startCol, UNKNOWN); }
                        else { current++; column++; token = new JavaToken(JavaTokenType.PERCENT, "%", start, startLine, startCol, UNKNOWN); }
                        break;
                    case '&':
                        if (peek(sourceCode, current, 1) == '&') { current += 2; column += 2; token = new JavaToken(JavaTokenType.AMPERSAND_AMPERSAND, "&&", start, startLine, startCol, UNKNOWN); }
                        else if (peek(sourceCode, current, 1) == '=') { current += 2; column += 2; token = new JavaToken(JavaTokenType.AMPERSAND_EQUALS, "&=", start, startLine, startCol, UNKNOWN); }
                        else { current++; column++; token = new JavaToken(JavaTokenType.AMPERSAND, "&", start, startLine, startCol, UNKNOWN); }
                        break;
                    case '|':
                        if (peek(sourceCode, current, 1) == '|') { current += 2; column += 2; token = new JavaToken(JavaTokenType.PIPE_PIPE, "||", start, startLine, startCol, UNKNOWN); }
                        else if (peek(sourceCode, current, 1) == '=') { current += 2; column += 2; token = new JavaToken(JavaTokenType.PIPE_EQUALS, "|=", start, startLine, startCol, UNKNOWN); }
                        else { current++; column++; token = new JavaToken(JavaTokenType.PIPE, "|", start, startLine, startCol, UNKNOWN); }
                        break;
                    case '^':
                        if (peek(sourceCode, current, 1) == '=') { current += 2; column += 2; token = new JavaToken(JavaTokenType.CARET_EQUALS, "^=", start, startLine, startCol, UNKNOWN); }
                        else { current++; column++; token = new JavaToken(JavaTokenType.CARET, "^", start, startLine, startCol, UNKNOWN); }
                        break;
                    case ':':
                        if (peek(sourceCode, current, 1) == ':') { current += 2; column += 2; token = new JavaToken(JavaTokenType.COLON_COLON, "::", start, startLine, startCol, UNKNOWN); }
                        else { current++; column++; token = new JavaToken(JavaTokenType.COLON, ":", start, startLine, startCol, UNKNOWN); }
                        break;
                    case '<':
                        if (peek(sourceCode, current, 1) == '=') { current += 2; column += 2; token = new JavaToken(JavaTokenType.LESS_EQUALS, "<=", start, startLine, startCol, UNKNOWN); }
                        else if (peek(sourceCode, current, 1) == '<') {
                            if (peek(sourceCode, current, 2) == '=') { current += 3; column += 3; token = new JavaToken(JavaTokenType.LEFT_SHIFT_EQUALS, "<<=", start, startLine, startCol, UNKNOWN); }
                            else { current += 2; column += 2; token = new JavaToken(JavaTokenType.LEFT_SHIFT, "<<", start, startLine, startCol, UNKNOWN); }
                        } else { current++; column++; token = new JavaToken(JavaTokenType.LESS_THAN, "<", start, startLine, startCol, UNKNOWN); }
                        break;
                    case '>':
                        if (peek(sourceCode, current, 1) == '=') { current += 2; column += 2; token = new JavaToken(JavaTokenType.GREATER_EQUALS, ">=", start, startLine, startCol, UNKNOWN); }
                        else if (peek(sourceCode, current, 1) == '>') {
                            if (peek(sourceCode, current, 2) == '=') { current += 3; column += 3; token = new JavaToken(JavaTokenType.RIGHT_SHIFT_EQUALS, ">>=", start, startLine, startCol, UNKNOWN); }
                            else if (peek(sourceCode, current, 2) == '>') {
                                if (peek(sourceCode, current, 3) == '=') { current += 4; column += 4; token = new JavaToken(JavaTokenType.UNSIGNED_RIGHT_SHIFT_EQUALS, ">>>=", start, startLine, startCol, UNKNOWN); }
                                else { current += 3; column += 3; token = new JavaToken(JavaTokenType.UNSIGNED_RIGHT_SHIFT, ">>>", start, startLine, startCol, UNKNOWN); }
                            } else { current += 2; column += 2; token = new JavaToken(JavaTokenType.RIGHT_SHIFT, ">>", start, startLine, startCol, UNKNOWN); }
                        } else { current++; column++; token = new JavaToken(JavaTokenType.GREATER_THAN, ">", start, startLine, startCol, UNKNOWN); }
                        break;
                    case '.':
                        if (peek(sourceCode, current, 1) == '.' && peek(sourceCode, current, 2) == '.') { current += 3; column += 3; token = new JavaToken(JavaTokenType.DOT_DOT_DOT, "...", start, startLine, startCol, UNKNOWN); }
                        else if (Character.isDigit(peek(sourceCode, current, 1))) { /* Let numeric logic handle it */ }
                        else { current++; column++; token = new JavaToken(JavaTokenType.DOT, ".", start, startLine, startCol, UNKNOWN); }
                        break;
                    default:
                        // No multi-char operator found
                        break;
                }
            }


            // --- 3. Literals and Single-character Punctuation/Operators ---
            if (token == null) {
                // TEXT BLOCK
                if (c == '"' && peek(sourceCode, current, 1) == '"' && peek(sourceCode, current, 2) == '"') {
                    current += 3; column += 3;
                    int endBlockIndex = sourceCode.indexOf("\"\"\"", current);

                    if (endBlockIndex != -1) {
                        String lexeme = sourceCode.substring(start, endBlockIndex + 3);

                        // Update position
                        for (int i = start + 3; i < endBlockIndex + 3; i++) {
                            char charAtI = sourceCode.charAt(i);
                            if (charAtI == '\n') { lineNumber++; column = 1; }
                            else if (charAtI == '\r') { /* Ignore \r if followed by \n */ }
                            else { column++; }
                        }

                        current = endBlockIndex + 3;
                        token = new JavaToken(JavaTokenType.TEXT_BLOCK_LITERAL, lexeme, start, startLine, startCol, UNKNOWN);
                    } else {
                        // Error: Text block not closed (Treat as spanning to EOF)
                        state.isInsideTextBlock = true;
                        token = new JavaToken(JavaTokenType.UNRECOGNIZED, sourceCode.substring(start), start, startLine, startCol, UNKNOWN);
                        current = sourceCode.length();
                        // Position tracking is simplified here since it's an error state
                    }
                }

                // IDENTIFIER or KEYWORD
                else if (Character.isLetter(c) || c == '_') {
                    while (current < sourceCode.length() &&
                           (Character.isLetterOrDigit(sourceCode.charAt(current)) || sourceCode.charAt(current) == '_')) {
                        current++;
                        column++;
                    }
                    String lexeme = sourceCode.substring(start, current);

                    if (lexeme.equals("_")) {
                        token = new JavaToken(JavaTokenType.RESERVED_UNDERSCORE, lexeme, start, startLine, startCol, UNKNOWN);
                    } else {
                        JavaTokenType type = KEYWORDS.getOrDefault(lexeme, JavaTokenType.IDENTIFIER);
                        token = new JavaToken(type, lexeme, start, startLine, startCol, UNKNOWN);
                    }
                }

                // NUMERIC LITERAL
                else if (Character.isDigit(c) || (c == '.' && Character.isDigit(peek(sourceCode, current, 1)))) {
                    // ... (Numeric literal consumption logic remains the same, ensuring current and column are updated)
                    boolean isFloat = (c == '.');

                    if (c == '0' && current + 1 < sourceCode.length()) {
                        char prefix = sourceCode.charAt(current + 1);
                        if (prefix == 'x' || prefix == 'X' || prefix == 'b' || prefix == 'B') {
                            current += 2; column += 2;
                            while (current < sourceCode.length() &&
                                   (Character.isDigit(sourceCode.charAt(current)) || sourceCode.charAt(current) == '_' ||
                                    ("xX".indexOf(prefix) != -1 && "abcdefABCDEF".indexOf(sourceCode.charAt(current)) != -1))) {
                                current++; column++;
                            }
                        } else if (Character.isDigit(prefix)) {
                            current++; column++;
                            while (current < sourceCode.length() && (Character.isDigit(sourceCode.charAt(current)) || sourceCode.charAt(current) == '_')) {
                                current++; column++;
                            }
                        }
                    }

                    while (current < sourceCode.length() &&
                           (Character.isDigit(sourceCode.charAt(current)) || sourceCode.charAt(current) == '_')) {
                        current++; column++;
                    }

                    if (peek(sourceCode, current, 0) == '.' && Character.isDigit(peek(sourceCode, current, 1))) {
                        isFloat = true;
                        current++; column++;
                        while (current < sourceCode.length() && Character.isDigit(sourceCode.charAt(current))) {
                            current++; column++;
                        }
                    }

                    if (peek(sourceCode, current, 0) == 'e' || peek(sourceCode, current, 0) == 'E') {
                        isFloat = true;
                        current++; column++;
                        if (peek(sourceCode, current, 0) == '+' || peek(sourceCode, current, 0) == '-') { current++; column++; }
                        while (current < sourceCode.length() && Character.isDigit(sourceCode.charAt(current))) {
                            current++; column++;
                        }
                    }

                    char suffix = peek(sourceCode, current, 0);
                    if (suffix == 'f' || suffix == 'F' || suffix == 'd' || suffix == 'D' || suffix == 'l' || suffix == 'L') {
                        current++; column++;
                        if (suffix == 'f' || suffix == 'F' || suffix == 'd' || suffix == 'D') {
                            isFloat = true;
                        }
                    }

                    String lexeme = sourceCode.substring(start, current);
                    token = new JavaToken(isFloat ? JavaTokenType.FLOAT_LITERAL : JavaTokenType.INTEGER_LITERAL, lexeme, start, startLine, startCol, UNKNOWN);
                }

                // // STRING LITERAL
                // else if (c == '"') {
                //     current++; column++;
                //     while (current < sourceCode.length() && sourceCode.charAt(current) != '"' && sourceCode.charAt(current) != '\n' && sourceCode.charAt(current) != '\r') {
                //         current++; column++;
                //     }
                //     if (current < sourceCode.length() && sourceCode.charAt(current) == '"') {
                //         current++; column++;
                //     }
                //     String lexeme = sourceCode.substring(start, current);
                //     token = new JavaToken(JavaTokenType.STRING_LITERAL, lexeme, start, startLine, startCol, UNKNOWN);
                // }
                // STRING LITERAL
                else if (c == '"') {
                    current++; column++;
                    while (current < sourceCode.length()) {
                        char currentCh = sourceCode.charAt(current);

                        // Handle escape sequences (e.g., \" or \\)
                        if (currentCh == '\\' && current + 1 < sourceCode.length()) {
                            current += 2;
                            column += 2;
                            continue;
                        }

                        // Stop if we hit the closing quote or a newline (unterminated string)
                        if (currentCh == '"' || currentCh == '\n' || currentCh == '\r') {
                            break;
                        }

                        current++;
                        column++;
                    }

                    if (current < sourceCode.length() && sourceCode.charAt(current) == '"') {
                        current++;
                        column++;
                    }

                    String lexeme = sourceCode.substring(start, current);
                    token = new JavaToken(JavaTokenType.STRING_LITERAL, lexeme, start, startLine, startCol, UNKNOWN);
                }

                // CHARACTER LITERAL
                else if (c == '\'') {
                    current++; column++;
                    while (current < sourceCode.length() && sourceCode.charAt(current) != '\'' &&
                           sourceCode.charAt(current) != '\n' && sourceCode.charAt(current) != '\r') {
                        if (sourceCode.charAt(current) == '\\' && current + 1 < sourceCode.length()) {
                            current += 2; column += 2;
                        } else {
                            current++; column++;
                        }
                    }
                    if (current < sourceCode.length() && sourceCode.charAt(current) == '\'') {
                        current++; column++;
                    }
                    String lexeme = sourceCode.substring(start, current);
                    token = new JavaToken(JavaTokenType.CHAR_LITERAL, lexeme, start, startLine, startCol, UNKNOWN);
                }

                // SINGLE PUNCTUATION/OPERATORS
                else {
                    switch (c) {
                        case '(': token = new JavaToken(JavaTokenType.LEFT_PAREN, "(", current, startLine, startCol, UNKNOWN); current++; column++; break;
                        case ')': token = new JavaToken(JavaTokenType.RIGHT_PAREN, ")", current, startLine, startCol, UNKNOWN); current++; column++; break;
                        case '{': token = new JavaToken(JavaTokenType.LEFT_BRACE, "{", current, startLine, startCol, UNKNOWN); current++; column++; break;
                        case '}': token = new JavaToken(JavaTokenType.RIGHT_BRACE, "}", current, startLine, startCol, UNKNOWN); current++; column++; break;
                        case '[': token = new JavaToken(JavaTokenType.LEFT_BRACKET, "[", current, startLine, startCol, UNKNOWN); current++; column++; break;
                        case ']': token = new JavaToken(JavaTokenType.RIGHT_BRACKET, "]", current, startLine, startCol, UNKNOWN); current++; column++; break;
                        case ';': token = new JavaToken(JavaTokenType.SEMICOLON, ";", current, startLine, startCol, UNKNOWN); current++; column++; break;
                        case ',': token = new JavaToken(JavaTokenType.COMMA, ",", current, startLine, startCol, UNKNOWN); current++; column++; break;
                        case '@': token = new JavaToken(JavaTokenType.AT_SIGN, "@", current, startLine, startCol, UNKNOWN); current++; column++; break;
                        case '~': token = new JavaToken(JavaTokenType.TILDE, "~", current, startLine, startCol, UNKNOWN); current++; column++; break;
                        case '?': token = new JavaToken(JavaTokenType.QUESTION_MARK, "?", current, startLine, startCol, UNKNOWN); current++; column++; break;
                        default:
                            current++; column++;
                            token = new JavaToken(JavaTokenType.UNRECOGNIZED, String.valueOf(c), current, startLine, startCol, UNKNOWN);
                            break;
                    }
                }
            }

            if (token != null) {
                if (verbose) {
                    System.out.println("  -> Found: " + token);
                }
                tokens.add(token);
            }
        }

        // Add EOF token
        tokens.add(new JavaToken(JavaTokenType.EOF, "", current, lineNumber, column, UNKNOWN));

        return tokens;
    }

    /**
     * Utility method to check if a token type is non-semantic (ignored by the parser).
     */
    private boolean isNonSemantic(JavaTokenType type) {
        return type == JavaTokenType.WHITESPACE ||
               type == JavaTokenType.SINGLE_LINE_COMMENT ||
               type == JavaTokenType.BLOCK_COMMENT_CONTENT;
    }
}
