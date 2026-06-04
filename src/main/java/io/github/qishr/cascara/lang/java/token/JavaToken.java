package io.github.qishr.cascara.lang.java.token;

import io.github.qishr.cascara.common.lang.token.Token;

public class JavaToken implements Token {

    private JavaTokenType type;
    private String lexeme;
    private int offset;
    private int line;
    private int column;
    private SemanticType semtype;

    public JavaToken(JavaTokenType type, String lexeme, int start, int startLine, int startCol, SemanticType unknown) {
        this.type = type;
        this.lexeme = lexeme;
        this.offset = start;
        this.line = startLine;
        this.column = startCol;
        this.semtype = unknown;
    }

    @Override
    public String toString() {
        return String.format("[%-18s | '%-15s' | L:%d C:%d]",
            type,
            // Simple escaping for display
            lexeme.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\\\""),
            line,
            column);
    }

    public JavaTokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getOffset() {
        return offset;
    }

    public int getStartLine() {
        return line;
    }

    public int getStartColumn() {
        return column;
    }

    public SemanticType getSemtype() {
        return semtype;
    }



    @Override
    public String getContent() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getValue'");
    }
}
