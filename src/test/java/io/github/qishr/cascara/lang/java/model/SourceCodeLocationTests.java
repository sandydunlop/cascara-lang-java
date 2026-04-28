package io.github.qishr.cascara.lang.java.model;

import org.junit.jupiter.api.Test;

import io.github.qishr.cascara.lang.java.model.SourceCodeLocation;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SourceCodeLocationTests {
    @Test
    void test() {
        SourceCodeLocation source = new SourceCodeLocation("/tmp/a.java", 10, 10);
        assertEquals("/tmp/a.java", source.getFileName());
        assertEquals(10, source.getLineNumber());
    }
}
