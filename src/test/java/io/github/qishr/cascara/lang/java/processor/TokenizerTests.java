package io.github.qishr.cascara.lang.java.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.qishr.cascara.common.diagnostic.Reporter;
import io.github.qishr.cascara.common.diagnostic.SimpleReporter;
import io.github.qishr.cascara.lang.java.token.JavaToken;

public class TokenizerTests {
    @Test
    void test_Tokenier() {
        Reporter reporter = new SimpleReporter();
        JavaTokenizer tokenizer = new JavaTokenizer().setReporter(reporter);

        // A double quoted string containing an escaped double quote
        // i.e. "\""
        String source = "\"\\\"\"";

        List<JavaToken> tokens =tokenizer.tokenize(source);
        String token0 = tokens.get(0).getLexeme();
        String token1 = tokens.get(1).getLexeme();
        assertEquals("\"\\\"\"", token0);
        assertEquals("", token1); // The EOF
        assertEquals(2, tokens.size()); // The string and the EOF
    }
}
